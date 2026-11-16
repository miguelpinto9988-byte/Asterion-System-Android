#!/usr/bin/env python3
"""Publica los 94 WebP aprobados usando GitHub Git Data API, sin tocar main.

Uso: GITHUB_TOKEN=<token> python3 scripts/upload_asterion_94_via_github_api.py paquete.zip
Requiere permiso 'Contents: Read and write' para este repositorio.
No compartas tu token en chats ni lo escribas dentro de este archivo.
"""
import base64, hashlib, json, os, sys, urllib.request, urllib.error, urllib.parse, zipfile
from concurrent.futures import ThreadPoolExecutor, as_completed

REPO="miguelpinto9988-byte/Asterion-System-Android"
BRANCH="design/profile-wizard-components-20261115"
ROOT="https://api.github.com/repos/"+REPO
PREFIX="app/src/main/res/drawable-nodpi/"

def git_sha(raw):
    return hashlib.sha1(b"blob "+str(len(raw)).encode()+b"\0"+raw).hexdigest()

def api(method, path, token, payload=None):
    body=None if payload is None else json.dumps(payload).encode()
    headers={"Authorization":"Bearer "+token,"Accept":"application/vnd.github+json",
             "X-GitHub-Api-Version":"2022-11-28"}
    if body is not None: headers["Content-Type"]="application/json"
    request=urllib.request.Request(ROOT+path,data=body,headers=headers,method=method)
    with urllib.request.urlopen(request, timeout=120) as response:
        return json.load(response)

def prepare(archive):
    with zipfile.ZipFile(archive) as z:
        records=json.loads(z.read("MANIFIESTO_ANDROID.json"))["assets"]
        if len(records)!=94: raise ValueError("Se requieren exactamente 94 WebP")
        plan={}
        for item in records:
            path=item["path"]
            if (not path.startswith(PREFIX) or not path.endswith(".webp")
                or "/" in path[len(PREFIX):] or "asterion_frame_" in path
                or "ic_marco." in path or path in plan):
                raise ValueError("Ruta inválida, duplicada o descartada: "+path)
            raw=z.read(path)
            if (len(raw)!=item["bytes"] or hashlib.sha256(raw).hexdigest()!=item["sha256"]
                or raw[:4]!=b"RIFF" or raw[8:12]!=b"WEBP"):
                raise ValueError("Integridad WebP incorrecta: "+path)
            plan[path]=(raw,git_sha(raw))
        return plan

def main():
    if len(sys.argv)!=2:
        raise SystemExit("Uso: python3 scripts/upload_asterion_94_via_github_api.py ARCHIVO.zip")
    plan=prepare(sys.argv[1])
    print("Verificados 94 WebP y sus SHA-256.",flush=True)
    token=os.environ.get("GITHUB_TOKEN") or os.environ.get("GH_TOKEN")
    if not token: raise ValueError("Falta GITHUB_TOKEN/ GH_TOKEN en el entorno")
    escaped=urllib.parse.quote(BRANCH,safe="")
    old=api("GET","/git/ref/heads/"+escaped,token)["object"]["sha"]
    base=api("GET","/git/commits/"+old,token)["tree"]["sha"]
    tree=api("GET","/git/trees/"+base+"?recursive=1",token)
    if tree.get("truncated"): raise ValueError("Árbol Git truncado")
    present={e["path"]:e["sha"] for e in tree["tree"] if e["type"]=="blob"}
    collision=[p for p,(_,sha) in plan.items() if p in present and present[p]!=sha]
    if collision: raise ValueError("No se sobrescribirá un archivo distinto: "+", ".join(collision))
    missing=[p for p,(_,sha) in plan.items() if present.get(p)!=sha]
    if not missing:
        print("Los 94 recursos ya están en GitHub, con SHA correcto.");return
    def upload(path):
        raw,sha=plan[path]
        result=api("POST","/git/blobs",token,{"encoding":"base64",
                   "content":base64.b64encode(raw).decode("ascii")})
        if result["sha"]!=sha: raise ValueError("SHA inesperado para "+path)
        return {"path":path,"mode":"100644","type":"blob","sha":sha}
    entries=[]
    with ThreadPoolExecutor(max_workers=5) as executor:
        for future in as_completed({executor.submit(upload,p):p for p in missing}):
            entries.append(future.result())
            print("Cargados:",len(entries),"/",len(missing),flush=True)
    if api("GET","/git/ref/heads/"+escaped,token)["object"]["sha"]!=old:
        raise ValueError("La rama cambió mientras se transferían los archivos.")
    newtree=api("POST","/git/trees",token,{"base_tree":base,"tree":entries})["sha"]
    commit=api("POST","/git/commits",token,{"message":
                 "assets(profile): incorporar 94 WebP cuadrados sin marcos",
                 "tree":newtree,"parents":[old]})["sha"]
    api("PATCH","/git/refs/heads/"+escaped,token,{"sha":commit,"force":False})
    final=api("GET","/git/trees/"+newtree+"?recursive=1",token)
    published={e["path"]:e["sha"] for e in final["tree"] if e["type"]=="blob"}
    if any(published.get(path)!=sha for path,(_,sha) in plan.items()):
        raise ValueError("Error de verificación posterior al commit")
    print("OK: 94 WebP verificados en GitHub. Commit: "+commit)
    print("https://github.com/"+REPO+"/pull/47")

if __name__=="__main__":
    try: main()
    except (ValueError,urllib.error.URLError,zipfile.BadZipFile) as exc:
        sys.exit("Error: "+str(exc))

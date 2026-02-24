# AKS Deployment (CryptoOrder)

## 1) Create/Update ConfigMap + Secret from `.env`
```bash
cd server/k8s/aks
NAMESPACE=cryptoorder APP_NAME=cryptoorder-server \
CONFIGMAP_NAME=cryptoorder-server-config SECRET_NAME=cryptoorder-server-secret \
JWT_SECRET_NAME=cryptoorder-server-jwt-keys \
./create-env-secret.sh
```

- Reads `.env` from the project root.
- Splits keys into ConfigMap/Secret.
- If `AUTH_JWT_PRIVATE_KEY_PATH` + `AUTH_JWT_PUBLIC_KEY_PATH` are set, creates JWT key Secret and mounts paths for Kubernetes (`/app/keys/*`).

## 2) Deploy to AKS
```bash
cd server/k8s/aks
IMAGE=2dtteam4temp.azurecr.io/cryptoorder-server:<tag> \
NAMESPACE=cryptoorder APP_NAME=cryptoorder-server \
CONFIGMAP_NAME=cryptoorder-server-config SECRET_NAME=cryptoorder-server-secret \
JWT_SECRET_NAME=cryptoorder-server-jwt-keys \
./deploy.sh
```

## 3) Verify
```bash
kubectl -n cryptoorder rollout status deployment/cryptoorder-server
kubectl -n cryptoorder get pods -l app=cryptoorder-server -o wide
kubectl -n cryptoorder logs deployment/cryptoorder-server --tail=200
```

## Optional knobs
- `CONTAINER_PORT`: defaults to `.env` `SERVER_PORT`, fallback `8080`
- `REPLICAS`: default `1`
- `HPA_ENABLED=true`: creates HPA (`HPA_MIN_REPLICAS`, `HPA_MAX_REPLICAS`, `HPA_CPU_UTILIZATION`)
- `CPU_REQUEST`, `MEMORY_REQUEST`, `CPU_LIMIT`, `MEMORY_LIMIT`

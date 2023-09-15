# install Nginx ingress controller on minikube 

```shell
minikube addons enable ingress
```

# create image to run locally

```shell
mvn clean package wildfly:image -Pprovisioned-server
```

run the non containerized server locally:

```shell
./target/server/bin/standalone.sh
```

```shell
curl http://localhost:8080/
```

# create image to run on kubernetes

```shell
mvn clean package wildfly:image -Popenshift
```

## push image into remote registry

```shell
podman login quay.io
podman tag wildfly-kubernetes-example quay.io/tborgato/wildfly-kubernetes-example
podman push quay.io/tborgato/wildfly-kubernetes-example
```

## install on kubernetes using helm

```shell
kubectl create namespace tborgato
kubectl config set-context --current --namespace=tborgato

helm repo add wildfly https://docs.wildfly.org/wildfly-charts/

helm install example-app wildfly/wildfly -f charts/values.yaml
```

## expose the service using an NodePort service

```shell
kubectl expose deployment example-app --type=NodePort --port=8080 --name=example-app-http

curl $(minikube service example-app-http --url -n tborgato)
```

## expose the service using an Ingress

```shell
kubectl apply -f ingress/example-app-ingress.yaml

echo "$(minikube ip) example-app.info" | sudo tee -a /etc/hosts

# http
curl http://example-app.info

# https
curl -k https://example-app.info
```
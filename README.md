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

# create image to run on openshift

```shell
mvn clean package wildfly:image -Popenshift
```

## push image into remote registry

```shell
podman login quay.io
podman tag wildfly-kubernetes-example quay.io/tborgato/wildfly-kubernetes-example
podman push quay.io/tborgato/wildfly-kubernetes-example
```

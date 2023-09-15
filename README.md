# Intro

This repo demonstrates running a clustered [WildFly](https://www.wildfly.org/) application on [Kubernetes - Minikube](https://minikube.sigs.k8s.io/docs/start/) using an [NGINX Ingress Controller](https://docs.nginx.com/nginx-ingress-controller/) to expose the application outside the Kubernetes cluster;
The [NGINX Ingress Controller](https://docs.nginx.com/nginx-ingress-controller/) is configured to support Sticky Sessions;

An Ingress Controller is a component in a Kubernetes cluster that configures an HTTP load balancer according to Ingress resources created by the cluster user.

Kubernetes uses a "Public Endpoint", which fronts the Ingress Controller pod(s). This is typically a TCP load balancer (cloud, software, or hardware) or a combination of such load balancer with a NodePort service. Clients connect to their applications via the Public Endpoint.

Read the following for a deeper understanding:
- [How NGINX Ingress Controller Works](https://docs.nginx.com/nginx-ingress-controller/intro/how-nginx-ingress-controller-works/)
- [Deploy on Kubernetes with Helm](https://www.wildfly.org/news/2023/06/16/deploy-on-kubernetes-with-helm/)
- [Nginx Ingress Controller in Kubernetes](https://medium.com/avmconsulting-blog/nginx-ingress-controller-in-kubernetes-8eb14f737f7b)
- [Session Affinity Using Nginx Ingress Controller: Kubernetes](https://medium.com/@ngbit95/session-affinity-using-nginx-ingress-controller-kubernetes-e39065e01a67)

# install Nginx ingress controller on minikube 

```shell
minikube addons enable ingress
```

after installation, you'll see one Pod named `ingress-nginx-controller-*` acting as load balancer:

```shell
$ kubectl get pods -n ingress-nginx
NAME                                        READY   STATUS      RESTARTS   AGE
ingress-nginx-admission-create-dsqt6        0/1     Completed   0          3h58m
ingress-nginx-admission-patch-zjdch         0/1     Completed   0          3h58m
ingress-nginx-controller-7799c6795f-sphr2   1/1     Running     0          3h58m
```

The Ingress Controller `ingress-nginx-controller-*` uses the Kubernetes API to get the latest `Ingress` resources created in the cluster and then configures `NGINX` according to those resources.

The Ingress Controller `ingress-nginx-controller-*` Pod consists of a single container, which in turn includes the following:

- IC process, which configures NGINX according to Ingress and other resources created in the cluster.
- NGINX master process, which controls NGINX worker processes.
- NGINX worker processes, which handle the client traffic and load balance the traffic to the backend applications.

```shell
$ kubectl get pod/ingress-nginx-controller-7799c6795f-sphr2 -n ingress-nginx -o yaml
apiVersion: v1
kind: Pod
metadata:
  creationTimestamp: "2023-09-15T09:03:01Z"
  generateName: ingress-nginx-controller-7799c6795f-
  labels:
    app.kubernetes.io/component: controller
    app.kubernetes.io/instance: ingress-nginx
    app.kubernetes.io/name: ingress-nginx
    gcp-auth-skip-secret: "true"
    pod-template-hash: 7799c6795f
  name: ingress-nginx-controller-7799c6795f-sphr2
  namespace: ingress-nginx
  ownerReferences:
  - apiVersion: apps/v1
    blockOwnerDeletion: true
    controller: true
    kind: ReplicaSet
    name: ingress-nginx-controller-7799c6795f
    uid: a9551808-cf05-4c69-bfe9-f339fe703a7b
  resourceVersion: "655"
  uid: 0ab73c34-370b-404b-a65e-425801b8d18c
spec:
  containers:
  - args:
    - /nginx-ingress-controller
    - --election-id=ingress-nginx-leader
    - --controller-class=k8s.io/ingress-nginx
    - --watch-ingress-without-class=true
    - --configmap=$(POD_NAMESPACE)/ingress-nginx-controller
    - --tcp-services-configmap=$(POD_NAMESPACE)/tcp-services
    - --udp-services-configmap=$(POD_NAMESPACE)/udp-services
    - --validating-webhook=:8443
    - --validating-webhook-certificate=/usr/local/certificates/cert
    - --validating-webhook-key=/usr/local/certificates/key
    env:
    - name: POD_NAME
      valueFrom:
        fieldRef:
          apiVersion: v1
          fieldPath: metadata.name
    - name: POD_NAMESPACE
      valueFrom:
        fieldRef:
          apiVersion: v1
          fieldPath: metadata.namespace
    - name: LD_PRELOAD
      value: /usr/local/lib/libmimalloc.so
    image: registry.k8s.io/ingress-nginx/controller:v1.8.1@sha256:e5c4824e7375fcf2a393e1c03c293b69759af37a9ca6abdb91b13d78a93da8bd
    imagePullPolicy: IfNotPresent
    lifecycle:
      preStop:
        exec:
          command:
          - /wait-shutdown
    livenessProbe:
      failureThreshold: 5
      httpGet:
        path: /healthz
        port: 10254
        scheme: HTTP
      initialDelaySeconds: 10
      periodSeconds: 10
      successThreshold: 1
      timeoutSeconds: 1
    name: controller
    ports:
    - containerPort: 80
      hostPort: 80
      name: http
      protocol: TCP
    - containerPort: 443
      hostPort: 443
      name: https
      protocol: TCP
    - containerPort: 8443
      name: webhook
      protocol: TCP
    readinessProbe:
      failureThreshold: 3
      httpGet:
        path: /healthz
        port: 10254
        scheme: HTTP
      initialDelaySeconds: 10
      periodSeconds: 10
      successThreshold: 1
      timeoutSeconds: 1
    resources:
      requests:
        cpu: 100m
        memory: 90Mi
    securityContext:
      allowPrivilegeEscalation: true
      capabilities:
        add:
        - NET_BIND_SERVICE
        drop:
        - ALL
      runAsUser: 101
    terminationMessagePath: /dev/termination-log
    terminationMessagePolicy: File
    volumeMounts:
    - mountPath: /usr/local/certificates/
      name: webhook-cert
      readOnly: true
    - mountPath: /var/run/secrets/kubernetes.io/serviceaccount
      name: kube-api-access-tnjmz
      readOnly: true
  dnsPolicy: ClusterFirst
  enableServiceLinks: true
  nodeName: minikube
  nodeSelector:
    kubernetes.io/os: linux
    minikube.k8s.io/primary: "true"
  preemptionPolicy: PreemptLowerPriority
  priority: 0
  restartPolicy: Always
  schedulerName: default-scheduler
  securityContext: {}
  serviceAccount: ingress-nginx
  serviceAccountName: ingress-nginx
  terminationGracePeriodSeconds: 0
  tolerations:
  - effect: NoSchedule
    key: node-role.kubernetes.io/master
    operator: Equal
  - effect: NoExecute
    key: node.kubernetes.io/not-ready
    operator: Exists
    tolerationSeconds: 300
  - effect: NoExecute
    key: node.kubernetes.io/unreachable
    operator: Exists
    tolerationSeconds: 300
  volumes:
  - name: webhook-cert
    secret:
      defaultMode: 420
      secretName: ingress-nginx-admission
  - name: kube-api-access-tnjmz
    projected:
      defaultMode: 420
      sources:
      - serviceAccountToken:
          expirationSeconds: 3607
          path: token
      - configMap:
          items:
          - key: ca.crt
            path: ca.crt
          name: kube-root-ca.crt
      - downwardAPI:
          items:
          - fieldRef:
              apiVersion: v1
              fieldPath: metadata.namespace
            path: namespace
status:
  conditions:
  - lastProbeTime: null
    lastTransitionTime: "2023-09-15T09:03:01Z"
    status: "True"
    type: Initialized
  - lastProbeTime: null
    lastTransitionTime: "2023-09-15T09:03:49Z"
    status: "True"
    type: Ready
  - lastProbeTime: null
    lastTransitionTime: "2023-09-15T09:03:49Z"
    status: "True"
    type: ContainersReady
  - lastProbeTime: null
    lastTransitionTime: "2023-09-15T09:03:01Z"
    status: "True"
    type: PodScheduled
  containerStatuses:
  - containerID: docker://79517d6cbefbd9224827632dbda4756bccf6cc42eb64834ad14ab3a1c6b93b23
    image: registry.k8s.io/ingress-nginx/controller@sha256:e5c4824e7375fcf2a393e1c03c293b69759af37a9ca6abdb91b13d78a93da8bd
    imageID: docker-pullable://registry.k8s.io/ingress-nginx/controller@sha256:e5c4824e7375fcf2a393e1c03c293b69759af37a9ca6abdb91b13d78a93da8bd
    lastState: {}
    name: controller
    ready: true
    restartCount: 0
    started: true
    state:
      running:
        startedAt: "2023-09-15T09:03:31Z"
  hostIP: 192.168.39.210
  phase: Running
  podIP: 10.244.0.5
  podIPs:
  - ip: 10.244.0.5
  qosClass: Burstable
  startTime: "2023-09-15T09:03:01Z"
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
podman tag wildfly-kubernetes-example:latest quay.io/tborgato/wildfly-kubernetes-example:latest
podman push quay.io/tborgato/wildfly-kubernetes-example:latest
```

## install on kubernetes using helm

```shell
kubectl create namespace tborgato
kubectl config set-context --current --namespace=tborgato

helm repo add wildfly https://docs.wildfly.org/wildfly-charts/

helm install example-app wildfly/wildfly -f charts/values.yaml
```

if you later make any modification, redeploy with:

```shell
helm upgrade example-app wildfly/wildfly -f charts/values.yaml
```

## expose the service using an NodePort service

```shell
kubectl expose deployment example-app --type=NodePort --port=8080 --name=example-app-http

curl $(minikube service example-app-http --url -n tborgato)
```

## expose the service using an Ingress

```shell
kubectl apply -f ingress/example-app-ingress.yaml -n tborgato

echo "$(minikube ip) example-app.info" | sudo tee -a /etc/hosts

# http
curl http://example-app.info

# https
curl -k https://example-app.info
```
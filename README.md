# Kubernetes demo

## useful resources:
* [One-page API Reference for Kubernetes v1.23](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.23/#deployment-v1-apps)
* [kubectl-commands](https://kubernetes.io/docs/reference/generated/kubectl/kubectl-commands)

## Steps to reproduce

### 02 - docker image

Build executable jar: `./gradlew build`

Build docker image: `docker build -t k8s-demo-02 .` 

### 03 - run image on local cluster imperatively

#### Run minikube cluster

Start minikube cluster: `minikube start`

Run dashboard: `minikube dashboard`

Get IP of minikube cluster: `minikube ip`

#### Run docker image imperatively

List docker images: `docker images`

Run image imperatively: `kubectl run demo-app --image=k8s-demo-02`

Get pods: `kubectl get pods`

Check what failed: `kubectl describe pod demo-app`

Delete pod: `kubectl delete pod demo-app`

#### Use minikube docker daemon

Set usage of minikube docker daemon: `eval $(minikube docker-env)`

Get docker images (from minikube daemon!): `docker images`

Build docker image: `docker build -t k8s-demo-02 .` 

OR:

Load docker image from local registry to minikube: `minikube image load k8s-demo-02`

Run image again with image-pull-policy: `kubectl run demo-app --image=k8s-demo-02 --image-pull-policy=Never`

Get logs from inside running pod: `kubectl logs demo-app`

Cleanup: `kubectl delete pod demo-app`

>**IfNotPresent**
the image is pulled only if it is not already present locally.

>**Always**
every time the kubelet launches a container, the kubelet queries the container image registry to resolve the name to an image digest. If the kubelet has a container image with that exact digest cached locally, the kubelet uses its cached image; otherwise, the kubelet pulls the image with the resolved digest, and uses that image to launch the container.

>**Never**
the kubelet does not try fetching the image. If the image is somehow already present locally, the kubelet attempts to start the container; otherwise, startup fails. See pre-pulled images for more details.

### 04 - expose service to localhost

Expose demo-app to localhost: `kubectl expose pod demo-app --port=8080 --type=LoadBalancer --name=demo-service`

Watch if service is getting the EXTERNAL-IP: `kubectl get service -w`

Open minikube tunnel (in new terminal window) to enable k8s LoadBalancer: `minikube tunnel`

Example of kubectl get service output:

```shell
NAME           TYPE           CLUSTER-IP     EXTERNAL-IP   PORT(S)          AGE
demo-service   LoadBalancer   10.99.200.65   127.0.0.1     8080:30081/TCP   4m4s
```

Use GET on our endpoint in a browser: `http://127.0.0.1:8080/`

Cleanup: `kubectl delete service demo-service` and `kubectl delete pod demo-app`

### 05 - create k8s Deployment in declarative way

While in k8s directory use: `kubectl apply -f 05-deployment.yaml`

In output of `kubectl get pods` we've got 2 instances as specified in deployment.yaml:

```shell
NAME                                   READY   STATUS    RESTARTS   AGE
demo-app-deployment-759999d8db-fqfm6   1/1     Running   0          4s
demo-app-deployment-759999d8db-sdkgn   1/1     Running   0          4s
```

Create a k8s Service in declarative way: `kubectl apply -f 05-service.yaml`

Get all k8s resources: `kubectl get all`

Use GET on our endpoint in a browser: `http://127.0.0.1:9376/`

#### Check how declarative way keeps the desired state:

Delete imperatively one pod: `kubectl delete pod <pod name>`

After `kubectl get pods` you can see that the new pods are automatically created:

```shell
NAME                                   READY   STATUS    RESTARTS   AGE
demo-app-deployment-86bc64cf6b-d7wbh   1/1     Running   0          6s
demo-app-deployment-86bc64cf6b-w4brn   1/1     Running   0          11m
```

#### Scale the deployment up/down:

Change `replicas: 2` to `replicas: 3` in 05-deployment.yaml, and apply it again.

```shell
polpc08778:k8s piotr.majorczyk$ kubectl apply -f deployment.yaml
deployment.apps/demo-app-deployment configured
polpc08778:k8s piotr.majorczyk$ kubectl get pods
NAME                                   READY   STATUS    RESTARTS   AGE
demo-app-deployment-86bc64cf6b-d7wbh   1/1     Running   0          3m27s
demo-app-deployment-86bc64cf6b-kw2pk   1/1     Running   0          6s
demo-app-deployment-86bc64cf6b-w4brn   1/1     Running   0          15m
```

Now change replicas count to 1 and apply changes again:
```shell
polpc08778:k8s piotr.majorczyk$ kubectl get pods
NAME                                   READY   STATUS    RESTARTS   AGE
demo-app-deployment-86bc64cf6b-d7wbh   1/1     Running   0          3m27s
demo-app-deployment-86bc64cf6b-kw2pk   1/1     Running   0          6s
demo-app-deployment-86bc64cf6b-w4brn   1/1     Running   0          15m
polpc08778:k8s piotr.majorczyk$ kubectl apply -f deployment.yaml
deployment.apps/demo-app-deployment configured
polpc08778:k8s piotr.majorczyk$ kubectl get pods
NAME                                   READY   STATUS    RESTARTS   AGE
demo-app-deployment-86bc64cf6b-w4brn   1/1     Running   0          16m
```

#### Check the load balancing feature

Now scale back the deployment to 3 instances.

Then run multiple time the `05.getPodName.sh` script and observe that the name of returned host are actually different and our requests were load balanced between multiple pods.

You can also use `<EXTERNAL-IP>:9376/host` endpoint (remember about caching mechanism!).

Cleanup: `kubectl delete service demo-app-service` and `kubectl delete deployment demo-app-deployment`

#### Clean all resources

Delete all objects: `kubectl delete daemonsets,replicasets,services,deployments,pods,rc,ingress --all --all-namespaces`

### 06 - Using Configmap

Apply all the files from `06-configmap`

Get into container: `kubectl exec --stdin --tty <pod name> -- /bin/bash`

Check that file-configuration was loaded: `ls config`

Check that simple value was loaded: `printenv <value kye>`

Check out the output of endpoint: `<EXTERNAL-IP>:9376/props`

#### Creating Configmap from directory/file/literals

Create configmap from directory: `kubectl create configmap dir-cm-example --from-file=./cm-files/`

>Note: When kubectl creates a ConfigMap from inputs that are not ASCII or UTF-8, the tool puts these into the binaryData field of the ConfigMap, and not in data

Run `kubectl describe configmap dir-cm-example`

```shell
Name:         dir-cm-example
Namespace:    default
Labels:       <none>
Annotations:  <none>

Data
====
date.properties:
----
day=20
month=04
year=2022
dummy.properties:
----
lorem=ipsum
foo=bar
geo.properties:
----
city=Poznan
country=Poland

BinaryData
====
.DS_Store: 6148 bytes
binary.zip: 242 bytes

Events:  <none>
```

Note that binary file(zip) data is put inside `BinaryData` section

---

Create configmap from single file: `kubectl create configmap file-cm-example --from-file=<file path>`

Create configmap from literals: `kubectl create configmap literals-cm-example --from-literal=first.key=value1 --from-literal=second.key=value2`

Save created Configmap to yaml: `kubectl get configmap literals-cm-example -o yaml > generated-configmap.yaml`

### 07 - Using Namespaces

Get all namespaces with labels: `kubectl get namespaces --show-labels`

```shell
polpc08778:06-configmap piotr.majorczyk$ kubectl get namespaces
NAME                   STATUS   AGE
default                Active   7d18h
kube-node-lease        Active   7d18h
kube-public            Active   7d18h
kube-system            Active   7d18h
kubernetes-dashboard   Active   7d18h
```
>Note: Namespaces starting with kube should not be used for user workload

>When you create a Service, it creates a corresponding DNS entry. This entry is of the form <service-name>.<namespace-name>.svc.cluster.local, which means that if a container only uses <service-name>, it will resolve to the service which is local to a namespace. This is useful for using the same configuration across multiple namespaces such as Development, Staging and Production. If you want to reach across namespaces, you need to use the fully qualified domain name (FQDN).

Find out which objects are in namespaces: `kubectl api-resources --namespaced=true`

#### Create Namespace
Create namespace imperatively: `kubectl create namespace <namespace name>`

OR

Create namespace declaratively: `kubectl apply -f our-namespace.yaml`

#### Create objects in Namespaces

To create an object in concrete namespace use -n flag, eg.: `kubectl apply -f 05-deployment.yaml -n <namespace name>`

>When no namespace is declared then the `default` namespace is used.

Alternatively we can specify the namespace inside the object definition itself. Eg.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: demo-app-deployment
  namespace: vivaldi
  labels:
    app: demo-app
```

### 08 - Using liveness and readiness probes

#### Readiness

After creating deployment run get pods with watch flag on: `kubectl get pods -w`

At first we can see that the created pod is not ready:

```shell
polpc08778:08-liveness&readiness piotr.majorczyk$ kubectl get pods -w
NAME                                   READY   STATUS    RESTARTS   AGE
demo-app-deployment-55d84fc556-wv48p   0/1     Running   0          4s
```

But 20 seconds after pod startup it status is changed to ready:

```shell
polpc08778:08-liveness&readiness piotr.majorczyk$ kubectl get pods -w
NAME                                   READY   STATUS    RESTARTS   AGE
demo-app-deployment-55d84fc556-wv48p   0/1     Running   0          4s
demo-app-deployment-55d84fc556-wv48p   1/1     Running   0          20s
```

Those 20 seconds comes from 2 facts:
- in out implementation we set the `/readiness` endpoint to return true 10 seconds after startup
- the period of readiness probe is set to default 10 seconds. So if we get false after initial 10 seconds then we will be able to get another result only after next 10 seconds.

#### Liveness (aka health check)

Check the pod: `kubectl get pods`

```shell
NAME                                   READY   STATUS    RESTARTS   AGE
demo-app-deployment-547f6c6d88-hsjj6   1/1     Running   0          2m50s
```

Run the `08.setUnhealthy.sh` script to change the HTTP code returned by `/health` endpoint: `curl --request POST --url 'localhost:9376/setUnhealthy'`

> Note: K8s treats 2xx and 3xx codes as "healthy" and everything else as "unhealthy"

Check how k8s behaves: `kubectl get pods -w`

```shell
polpc08778:08-liveness&readiness piotr.majorczyk$ kubectl get pods -w
NAME                                   READY   STATUS    RESTARTS   AGE
demo-app-deployment-547f6c6d88-hsjj6   1/1     Running   0          7m21s
demo-app-deployment-547f6c6d88-hsjj6   0/1     Running   1 (1s ago)   7m26s
demo-app-deployment-547f6c6d88-hsjj6   1/1     Running   1 (20s ago)   7m45s
```

As we can see the k8s liveness probe fails and k8s tries to restart the unhealthy pod. After the restart the variable returned by `/health` endpoint is again true and service is again healthy.

Check the logs of previous (restarted pod): `kubectl logs demo-app-deployment-b96d94779-wwn97 --previous`

and we can see in logs:
```shell
Health set to failing
2022-03-21 23:39:51.299  WARN 1 --- [nio-8080-exec-5] c.m.p.k.KubernetesDemoApplication        : Service unhealthy! Try: 1
2022-03-21 23:39:56.299  WARN 1 --- [nio-8080-exec-6] c.m.p.k.KubernetesDemoApplication        : Service unhealthy! Try: 2
2022-03-21 23:40:01.297  WARN 1 --- [nio-8080-exec-8] c.m.p.k.KubernetesDemoApplication        : Service unhealthy! Try: 3
2022-03-21 23:40:06.298  WARN 1 --- [nio-8080-exec-9] c.m.p.k.KubernetesDemoApplication        : Service unhealthy! Try: 4
2022-03-21 23:40:11.281  WARN 1 --- [nio-8080-exec-1] c.m.p.k.KubernetesDemoApplication        : Service unhealthy! Try: 5
2022-03-21 23:40:16.281  WARN 1 --- [nio-8080-exec-2] c.m.p.k.KubernetesDemoApplication        : Service unhealthy! Try: 6
2022-03-21 23:40:21.280  WARN 1 --- [nio-8080-exec-3] c.m.p.k.KubernetesDemoApplication        : Service unhealthy! Try: 7
2022-03-21 23:40:26.280  WARN 1 --- [nio-8080-exec-5] c.m.p.k.KubernetesDemoApplication        : Service unhealthy! Try: 8
```
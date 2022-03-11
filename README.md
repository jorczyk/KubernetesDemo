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

Use GET on our endpoint in a browser: `http://127.0.0.1:9376/`

#### Check how declarative way keeps the desired state:

Delete imperatively one pod: `kubectl delete pod <pod name>`

After `kubectl get pods` the new pods is automatically created:

```shell
NAME                                   READY   STATUS    RESTARTS   AGE
demo-app-deployment-86bc64cf6b-d7wbh   1/1     Running   0          6s
demo-app-deployment-86bc64cf6b-w4brn   1/1     Running   0          11m
```

#### Scale the deployment up/down:

Change `replicas: 2` to `replicas: 3` in 05-deployment.yaml, and apply it again.

```shell
polpc08778:k8s piotr.majorczyk$ kubectl apply -f 05-deployment.yaml
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
polpc08778:k8s piotr.majorczyk$ kubectl apply -f 05-deployment.yaml
deployment.apps/demo-app-deployment configured
polpc08778:k8s piotr.majorczyk$ kubectl get pods
NAME                                   READY   STATUS    RESTARTS   AGE
demo-app-deployment-86bc64cf6b-w4brn   1/1     Running   0          16m
```

Cleanup: `kubectl delete service demo-app-service` and `kubectl delete deployment demo-app-deployment`
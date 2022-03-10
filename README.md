# Kubernetes demo

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

>**IfNotPresent**
the image is pulled only if it is not already present locally.

>**Always**
every time the kubelet launches a container, the kubelet queries the container image registry to resolve the name to an image digest. If the kubelet has a container image with that exact digest cached locally, the kubelet uses its cached image; otherwise, the kubelet pulls the image with the resolved digest, and uses that image to launch the container.

>**Never**
the kubelet does not try fetching the image. If the image is somehow already present locally, the kubelet attempts to start the container; otherwise, startup fails. See pre-pulled images for more details.
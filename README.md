# Hello Mesh
This is the simplest example app using the mesh framework - it shows how to discover other users 
using the same app, send and receive data on the mesh and configure the role within the mesh 
(forwarder or not, internet sharer or not)

## How does it work?
The RightMesh library has an autonomous networking layer which manages the connectivity between 
devices using Wi-Fi, Bluetooth and Wi-Fi direct. It does this by linking together hotspots using
our patent-pending switching technology.

In addition to helping the devices make the physical connections to each other, RightMesh implements
a neighbour discovery protocol which allows devices to discover each other across many hops.

As a developer you will receive events when other devices running the same app join the network
(even if they are connected through devices which are running different apps). You will also receive
an event when data is received from another device running the same app as you. 

RightMesh abstracts away the idea of IPv4, IPv6 addresses, MAC addresses etc., since any given
device may have any number of connections into the mesh at a given moment. Instead every device has
a MeshID which is used in place of these other types of addresses. 

The MeshID is actually an Ethereum compatible account which will be used very soon to keep track
of how much data has been forwarded and received so that people can be incentivized to use the mesh.

You can send byte arrays of data to other mesh devices and RightMesh will handle reliable
communication for you. While the library does it's best to ensure connectivity to as many devices
as possible at all times, the mobile nature of the devices means they may disconnect from each other
. RightMesh implements networking layers to handle this enabling reliable congestion-aware end-to-end
communications. Compared to similar libraries, RightMesh does not broadcast to all other devices in
the mesh, it actually forms paths and performs routing using this two-layer system.

For the developer, this means when they issue a send call, they can trust that the data will be
received on the other side, even when the network grows in size, and despite the mobile nature of
the devices.

## Documentation
Our API reference is available at [https://developer.rightmesh.io/api/](https://developer.rightmesh.io/api/)

A detailed step-by-step breakdown of how to get started can be found in our reference guide: [https://developer.rightmesh.io/reference/](https://developer.rightmesh.io/reference/)

In order for this sample app to work, you need to obtain RightMesh developer account, and API key
from our developer website: [https://developer.rightmesh.io/](developer.rightmesh.io)

Set your username, password and key in the app [build.gradle](app/build.gradle) file. The main
source code is available in [MainActivity.java](app/src/main/java/io/left/hellomesh/MainActivity.java)
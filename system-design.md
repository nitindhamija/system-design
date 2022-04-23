# study material refernces

gaurav sen

# horizontal scaling

is adding more resources i.e in case of aws ec2 instances of 1cpu 2 gb ram adding more ec2 instances to handle the load so in essence we add more resources to handle the increased load.

# vertical scaling

is increasing the size of cpu and ram to handle the increased load, so basically it is buying a bigger maching in case of aws ec2 instace switching from low config t.2.micro cpu 1 ram 2gb ram to high conifg large cpu 16, ram 64 gb ec2 instrances.

# load balancing

is the technique to balance/distribute the load among the backend servers i.e ec2 instances

## hashing

is one of algorithm used by load balancers to distribute the load among the servers.

here let's say we have 4 servers s1,s2,s3,s4
we apply hashing on incoming req to decide which server should handle it.

hash(req_id/user_id) %4
in general hash(req_id/user_id) % N in case of n servers

here hash function parameter could any of the incoming request not specifically req_id it could user_id, name or whatever unique identifier for that request.

few examples

req 1 -> hash(1)%4 = 1 req goes to server no 1
req 6 -> hash(1)%4 = 2 req goes to server no 2
req 100 -> hash(1)%4 = 0 req goes to server no 0
and so on

and it disctributes the load evenly amoung the backend servers i.e load factor 1/N

examine case below.

req 56 -> hash(123)%4 = 3 req goes to server no 3 where 123 is user id
now we know that this req will always go to server no 3 since hash is constant function and user_id will remain the same so instead of doing operations at server like searching the database or any other operation again and again we can have cache at server and for the same req we can send the cached data which can improve the performace by reducing the response time.

### problem with this configuration

refer the links below if do not understand the problem
https://michaelnielsen.org/blog/consistent-hashing/

Now if think about the caching scenario above the problem with this configuration is when we add and or remove the servers.
let's say i add another server 5 now many of the request which were going to let's s1 will go to s2 now and many of the req going to s2 will now go to s3 and same for rest of the servers so their caches stored at old servers will not be useful anymore.

since there are 5 servers the load will 20% each so now observer below the change impact

s1 5% load will go to s2 and
s2 will have 5% new load from s1 i.e to build new cache
s2 10 % load will go s3
s3 will have 10% new load from s2 i.e to build new cache
s3 15% load will go to s4
s4 will have 15% new load from s3 i.e to build new cache
s4 20% load will go to s5
s4 will have 20% new load from s4 i.e to build new cache

so in this case 5+5+10+10+15+15+20+20 = 100%
change impact is 100% i.e almost all the cache at instance level will be useless now.
so consistent hashing will solve this problem

#### another example of problem

Naive hash-based distributed dictionaries are simple, but they have serious limitations. Imagine you’re using a cluster of computers to crawl the web. You store the results of your crawl in a distributed dictionary. But as the size of the crawl grows, you’ll want to add machines to your cluster. Suppose you add even just a single machine. Instead of computing hash(k)\mod(n) we’re now computing hash(k)\mod(n+1). The result is that each key-value pair will get reallocated completely at random across the cluster. You’ll end up moving a fraction n/(n+1) of your data to new machines – i.e., nearly all of it. This will be slow, and might potentially be expensive. It’s also potentially inconvenient if jobs are being carried out. Similar problems arise if you add a larger block of machines to the cluster, or if you lose some machines (e.g., if some of the machines fail). You get the idea.

## consistent hashing

to understand refer the image below and external links

now to solve this problem with adding and removing servers in normal hashing, an algorithm called consisten hashing is used

let's say there are n servers and each server has a server id e.g 223
now imagine a ring with m points ranging from 0,1,2,.... m-1 in clockwise direction
ow hash(server_id)% m gives us the location in ring where server will be placed i.e that no from the ring can be assigned to the server
so for 4 servers will be distributed uniformally in the ring
now req using the hashing {hash(req_id/user_id)/4 } will give some point location in the ring, now request from that point will go to nearest server in clockwise direction now with this configuration ideally load will distributed uniformly but if server count is very small then we might skewed configuration where load is not uniform across the servers, so to tackle this problem we apply multiple hashing function h1,h2...hk i.e k hashing function on sever id to place the servers in the ring but how can one server be placed in multiple point location in the ring it can't it's virtual i.e all those points are mapped to server e.g 3 points across the ring are mapped to s1, 3 for s2 and so on so this way when we add or remove servers we actually add or remove multiple points in ring and no of requests impacted are less and distributed uniformly to remaing servers since virtual points are spread across the ring

change will be 1/(n+1) for adding a server which quite low so it quite effective technique and used in lot of places web caches, databases, memcahed, Amazon’s Dynamo key-value store etc.

![](notes_images/systemdesign1.jpg)

### links for guide and code

https://github.com/coding-parrot/Low-Level-Design/blob/master/service-orchestrator/src/main/java/algorithms/ConsistentHashing.java

https://michaelnielsen.org/blog/consistent-hashing/

### code walkthrough

if you look at the code hash function is what will gives the point locations on the ring where server will placed logically and nodepositions map contains the node and it's corresponding all postion i.e node1,[1,11,41] etc
nodemapping map contains the mapping of point locations and nodes in sorted order of point locations
sorting here is for clockwise direction navigation

and we need three functions here one to add node, remove node and getassigned node

now pointmultiplier specifies how many virtual positions we want for a server if we want 2 virtual positions that way node mapping will have enteries like
node1->[1,31]
node2->[11,41]

in add function
loop the pointmultiplier times i.e 2 times apply the hash function on server/node (i \* pointmultiplier)+ nodeid to get the actual postion on ring and then store the result to nodemappings map and nodeposition map

in remove function remove the node from nodepositions and then all positions from node mappings for the node

in get assigned node function

apply the hash function to req to get the position on ring and then nodemapping.higherEntry(req_position) will give the node which will handle the request

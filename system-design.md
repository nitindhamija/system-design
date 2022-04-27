# Table of contents
- [Table of contents](#table-of-contents)
- [study material refernces](#study-material-refernces)
- [HorizontalScaling](#horizontalscaling)
- [vertical scaling](#vertical-scaling)
- [LoadBalancing](#loadbalancing)
  - [hashing](#hashing)
    - [problem with this configuration](#problem-with-this-configuration)
      - [another example of problem](#another-example-of-problem)
  - [ConsistentHashing](#consistenthashing)
    - [links for guide and code](#links-for-guide-and-code)
    - [code walkthrough](#code-walkthrough)
- [MessagingQueue](#messagingqueue)
  - [TODO](#todo)
  - [reference_links](#reference_links)
  - [use_cases](#use_cases)
    - [a_simple_use_case](#a_simple_use_case)
    - [use_case_Imaging_service](#use_case_imaging_service)
    - [use_case_email_service](#use_case_email_service)
    - [use_case_with_problem_and_need_of_message_queue](#use_case_with_problem_and_need_of_message_queue)
  - [General Scenario for using message queue](#general-scenario-for-using-message-queue)
  - [comparsion_of_diff_message_queue](#comparsion_of_diff_message_queue)
    - [zeroMQ_vs_RabbitMQ/ActiveMQ](#zeromq_vs_rabbitmqactivemq)
    - [RabbitMQ_vs_ActiveMQ](#rabbitmq_vs_activemq)
  - [inter-service communication vs job processing use case](#inter-service-communication-vs-job-processing-use-case)
  - [installation on windows](#installation-on-windows)
- [database design choosing the right database](#database-design-choosing-the-right-database)
- [choosing structured or non structured database](#choosing-structured-or-non-structured-database)
- [amazone system design (ecommerce system design)](#amazone-system-design-ecommerce-system-design)
  - [Functional Requirements](#functional-requirements)
  - [Non Functional Requirements](#non-functional-requirements)
- [TODO](#todo-1)


# study material refernces

gaurav sen

# HorizontalScaling

is adding more resources i.e in case of aws ec2 instances of 1cpu 2 gb ram adding more ec2 instances to handle the load so in essence we add more resources to handle the increased load.

# vertical scaling

is increasing the size of cpu and ram to handle the increased load, so basically it is buying a bigger maching in case of aws ec2 instace switching from low config t.2.micro cpu 1 ram 2gb ram to high conifg large cpu 16, ram 64 gb ec2 instrances.

# LoadBalancing

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

## ConsistentHashing

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

# MessagingQueue

In modern cloud architecture, applications are decoupled into smaller, independent building blocks that are easier to develop, deploy and maintain. Message queues provide communication and coordination for these distributed applications. Message queues can significantly simplify coding of decoupled applications, while improving performance, reliability and scalability.

Message queues allow different parts of a system to communicate and process operations asynchronously. A message queue provides a lightweight buffer which temporarily stores messages, and endpoints that allow software components to connect to the queue in order to send and receive messages. The messages are usually small, and can be things like requests, replies, error messages, or just plain information. To send a message, a component called a producer adds a message to the queue. The message is stored on the queue until another component called a consumer retrieves the message and does something with it.

![](img/amq.jpg)

Many producers and consumers can use the queue, but each message is processed only once, by a single consumer. For this reason, this messaging pattern is often called one-to-one, or point-to-point, communications. When a message needs to be processed by more than one consumer, message queues can be combined with Pub/Sub messaging in a fanout design pattern
refer https://aws.amazon.com/pub-sub-messaging/ for more details
Amazone SNS is one of example of pub/sub model

## TODO

- check usagae in portex
- how it actually works(LLD)

## reference_links

- http://blog.codepath.com/2013/01/06/asynchronous-processing-in-web-applications-part-2-developers-need-to-understand-message-queues/

- https://www.youtube.com/watch?v=oUJbuFMyBDk&list=PLMCXHnjXnTnvo6alSjVkgxV-VH6EPyvoX&index=5

- https://www.cloudamqp.com/blog/what-is-message-queuing.html

## use_cases

### a_simple_use_case

Imagine that you have a web service that receives many requests every second, where no request can get lost, and all requests need to be processed by a function that has a high throughput. In other words, the web service always has to be highly available and ready to receive a new request instead of being locked by the processing of previously received requests.

In this case, placing a queue between the web service and the processing service is ideal. The web service can put the "start processing" message on a queue and the other process can take and handle messages in order. The two processes are decoupled from each other and do not need to wait. If you have a lot of requests coming in a short amount of time, the processing system will be able to process them all. The queue will persist with the requests even if their number grows.

Then imagine that the business and workload are growing and the system needs to be scaled up. All that needs to be done is to add more consumers to work off the queues faster.

![](img/mq.jpg)
![](img/mq1.jpg)

### use_case_Imaging_service

Another use case could be when working on an imaging system where people upload images and then you have a service that generates thumbnails for each uploaded message, in this case, your best solution would be to implement the concept of “message queue.

### use_case_email_service

email communication is also a use case for Messgaing Queue where sender sends the mail and an immediate response is message sent to sender(msg has been sent), however the email is placed on a queue which is then process later by consumer component of the service and once it is processed by the consumer then reciever recieves the mail so here producer and sender are communicating asynchronously and neither one waits for another and decoupled from each other. Also if the message is failed for any reasone you get the response after some time

### use_case_with_problem_and_need_of_message_queue

consider the popular pizza shop/server like dominoes, now let's assume there are 3 shops serving orders now if one shops goes down i.e it becomes unable to serve and inprogress orders also needs to rerouted to other shops now for this we need persistence storage of order information.

let's assume a persistence storage with order information like order ip and server serving that order with server id

now we can find all the unfinished order from down server and distibute those orders to remaining shops/server via load balancing(consistent hashing) however here we also need a heartbeat check service(notifier) which whose job is to keep checking is the server is alive i.e every 5 sec or 10 sec and to notify of the changes etc

so in essence we need a hearbeat check service + persistent storage + load balancing(consistent hashing)
and a message queue is what has all these features like rabbitMQ,Aapache ActiveMQ,ZeroMQ JMS etc

## General Scenario for using message queue

- Servers are processing jobs in parallel.
- A server can crash. The jobs running on the crashed server still needs to get processed.
- A notifier constantly polls the status of each server and if a server crashes it takes ALL unfinished jobs (listed in some database) and distributes it to the rest of the servers. Because distribution uses a load balancer (with consistent hashing) duplicate processing will not occur as job_1 which might be processing on server_3 (alive) will land again on server_3, and so on.
- This "notifier with load balancing" is a "Message Queue".

## comparsion_of_diff_message_queue

![](img/mqs.jpg)

### zeroMQ_vs_RabbitMQ/ActiveMQ

The interesting thing to understand is that ZeroMQ is actually not so much a pre-packaged message queue like the others but instead acts as a framework for building message queues.ZeroMQ focuses mostly on just passing the messages very efficiently over the wire while RabbitMQ acts as a full-fledged ‘broker’ which handles persisting, filtering and monitoring messages.
Think of ZeroMQ as it’s own toolbox or framework for creating message queues tailored to your own needs

- Of course, with RabbitMQ or ActiveMQ, the broker and persistence built in adds quite a bit of overhead but those libraries choose to sacrifice raw speed to provide a much richer feature set with less manual tinkering.
- choose ZeroMQ if you are looking for more control, raw speed and are interested in a light-weight, do-it-yourself protocol for delivering messages. In other cases where you just want to use a queue for typical use cases and you are willing to accept the higher overhead, you should consider RabbitMQ or ActiveMQ.

### RabbitMQ_vs_ActiveMQ

ActiveMQ is built in Java on the JMS (Java Message Service) and is very frequently used within applications on the JVM (Java, Scala, Clojure, et al).ActiveMQ also supports STOMP which provides support for Ruby, PHP and Python.RabbitMQ is built on Erlang, powered by AMQP and is used frequently with applications within Erlang, Python, PHP, Ruby, et al.
the configuration for ActiveMQ is in XML and the routing of messages is handled with custom rules defined by ActiveMQ. In contrast, the configuration of RabbitMQ is through an Erlang syntax and the advanced routing and configuration follows standard AMQP specifications. The protocols (AMQP vs JMS) used by each queue have certain underlying differences as well. One key difference is that in AMQP a producer sends a message to the broker without knowing the intended distribution strategy while in JMS the producer is aware of the strategy to be used explicitly.

## inter-service communication vs job processing use case

![](img/mq_use_Cases.jpg)
For the average web application though, the requirements are very different. To understand your requirements, ask yourself if you are sending messages to communicate between different services in your application or if you just want to process simple background jobs. In the latter case, we certainly don’t need the most powerful or flexible message queue nor the expensive associated setup costs.
Most popular web applications really only need a way to do background job processing and offload tasks to an asynchronous queue. These more specific and constrained requirements open up the possibility for a lighter-weight message queue that is easier to use and focused on doing one thing well.

## installation on windows

https://www.rabbitmq.com/install-windows-manual.html
https://www.rabbitmq.com/tutorials/tutorial-six-java.html

# database design choosing the right database

- choice of data generally depends
  - structucted or not non structured data
  - the kind of query pattern
  - the amount of scale you want apply to the db

- for caching solution you can use key paid database redis
- where we want to store image/video kind of data i.e need of various OTT like netflix amazone prime hotstar  we need blob storage and there are various providers for blob storage service amazone s3 is one of best and cheap bloc storage service, And generally along with s3 it is good practice to use CDN so that all your content is distributed geographically to the edge locations and so can be served with low latency depeneding on where the user is requesting the video from.
- if you want text searching capabilites like searhcing movies in netflix or searching items in amazone you need something like text search engine elastic search and solr are two such solutions however these are not database these are search engine diff is database ensures consistenty about the data but search engine don't so these should not be used as primary source of data.Also they support fuzzy search as well now when you type airprot mistakenly on netflix still airport comes in result, you know how? well there's change of only 2 character i.e if r and o switch position it becomes airport i.e that edit distance is 2 i.e swicthing 2 character gives a word so that is what fuzzy search is
- when we have metrics kind of data like cpu, memory untilization then we don't do random updates we do sequential updates like data at t1 is appended before data at t2 and so on and read queries are king od bulk head like last 10 min or data 1 hour , last week so time series databases are optimized for these kind of input and output pattern influxDB and openTSDB are some of the example of Time Series db
# choosing structured or non structured database 
 ![](img/db_design.jpg)
 ![](img/acid.jpg)
  - so if you want structured data and data should follow acid property then go for RDBMS(orange, mysql, postgres etc)
  - however if you want structured data but acid property are not mandatory then you can use either RDBMS or no sql db like mongo db
  - if you have unstructured data like catalogue items in amazone now item can be shirt with size and color attribute and item could refrigerator with attribute volume, weight and query pattern could be large since there would be so many attributes of diff catalogue items then you can use document db, mongodb and couchbase are 2 such kind of db.
  - if you have unstructured data and data is ever increasing but query pattern are limited then you use columnar db cassandra is one such db take example of uber where drivers are sending their locations every day and with new onboards it keeps on increasing so it is an ever increasing data and queries would be finite like which driver is near to this user locations etc

# amazone system design (ecommerce system design)
## Functional Requirements
- search(should also say whether items is available and can be delivered or not at front and not at the time of checkout)
- cart/wishlist
- checkout
- view order
  
 ## Non Functional Requirements
 - low latency(required for catalogue search, not for inventory update etc)
 - high availability(required for catalogue search)
 - high consistency (required for inventory and order service, payment service)

![](img/amazone_design.jpg)
![](img/amazone_sd1.jpg)
![](img/amazone_sd2.jpg)
# TODO
- read about kafka
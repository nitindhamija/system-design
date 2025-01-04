# resources
- youtube neetcode 
- exponent youtube channel mock interviews
- code karle youtube 
- alex xu system design
- grokking system design 
# streaming system like netflix, hotstar, amazone , youtube etc
- imptortant things to cover
- content upload (splitting and aggregating using mapreduce, encoding, async message queue, s3 object storage, ratelimiting)
- search (search service, cache, metadata db)
- watch/view content (CDN, viewing service, cache metadata db, s3)
- for upload api details refer grokking system design book

## where to store thumbnails
- There will be a lot more thumbnails than videos. If we assume
that every video will have five thumbnails, we need to have a very efficient storage system that can
serve a huge read traffic. There will be two consideration before deciding which storage system should
be used for thumbnails:
1. Thumbnails are small files with, say, a maximum 5KB each.
2. Read traffic for thumbnails will be huge compared to videos. Users will be watching one video
at a time, but they might be looking at a page that has 20 thumbnails of other videos.
Let’s evaluate storing all the thumbnails on a disk. Given that we have a huge number of files, we have
to perform a lot of seeks to different locations on the disk to read these files. This is quite inefficient
and will result in higher latencies.
Bigtable can be a reasonable choice here as it combines multiple files into one block to store on the
disk and is very efficient in reading a small amount of data. Both of these are the two most significant
requirements of our service. Keeping hot thumbnails in the cache will also help in improving the
latencies and, given that thumbnails files are small in size, we can easily cache a large number of such
files in memory.

![my design](netflix-youtube-prime-ott-system-design.drawio)

## do read about splitting mapreduce and ecoding IMP
- from gaurav sen youtube video on encoding of videos in diff formats and resolution
- encoding process is detailed in alexu system design vol 1 so read it from there


## how and for what purpose netflix uses big data and use tools like apache spark 

## check out netflix tech stack by bytebytego 
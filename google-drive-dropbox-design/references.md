# resources
- refer grokking system design as this has the best design in terms of client app role and overall 
- alex xu system design
- https://medium.com/@narengowda/system-design-dropbox-or-google-drive-8fd5da0ce55b
 
# imptortant things to cover
- file upload (splitting/chunking, compression & encryption, s3 object storage, ratelimiting)
- edit/update (delta sync i.e only upload modyfing chunks and not the whole file)
- download & sync across multple client (CDN, s3 storage bucket, cache, metadata db, s3)


# does google drives uses client side chunking for large file upload or server side or no chunking at all
as per chatgpt client side chunking is done
## file upload via chunking
- read from system design grokking
- https://stackoverflow.com/questions/50121917/split-an-uploaded-file-into-multiple-chunks-using-javascript
- using blob.slice() at client side we can break file in to chunks and upload it
- another lib plupload chunks the files and upload
While providing file upload and sync as service it is not a good idea to save the file as a whole. the reasons being

- Bandwidth and cloud space utilization
- Latency or Concurrency utilization: It takes more time to upload single file as a whole.
  
- So the better model is to break the files in to multiple chunks and then its easier to upload, save and keep multiple version of files by just saving the chunks which are updated upon file update. These chunks are named by the hash of the chunk’s content itself.
- We also need to store all the chunks names and order(metadata) information to recreate the file using chunks when we download/Sync.

![my design](google-drive-dropbox.drawio)

## chunking should be done at client side or server side
- ans is both for web client we can do chunking, delta sync, compression at block servers 
- and for client app it can be done at client side taking advantage of user's device
 
## client app
Based on the above considerations, we can divide our client into following four parts:
- I. Internal Metadata Database will keep track of all the files, chunks, their versions, and their
location in the file system.Server and clients can calculate a hash (e.g., SHA-256) to see
whether to update the local copy of a chunk or not. On the server, if we already have a chunk with a
similar hash (even from another user), we don’t need to create another copy, we can use the same
chunk.
- II. Chunker will split the files into smaller pieces called chunks. It will also be responsible for
reconstructing a file from its chunks. Our chunking algorithm will detect the parts of the files that have
been modified by the user and only transfer those parts to the Cloud Storage; this will save us
bandwidth and synchronization time.
- III. Watcher will monitor the local workspace folders and notify the Indexer (discussed below) of any
action performed by the users, e.g. when users create, delete, or update files or folders. Watcher also
listens to any changes happening on other clients that are broadcasted by Synchronization service.
- IV. Indexer will process the events received from the Watcher and update the internal metadata
database with information about the chunks of the modified files. Once the chunks are successfully
submitted/downloaded to the Cloud Storage, the Indexer will communicate with the remote
Synchronization Service to broadcast changes to other clients and update remote metadata database.

# further dicussion 
to prevent DDOS attach we can implement rate limiter service which will rate limit req to block servers and api servers
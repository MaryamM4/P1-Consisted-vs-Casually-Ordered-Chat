# Consistent Ordering
In consistent ordering, all nodes receive messages in the same order (even if the order is incorrect). 
It is implemented by having all clients send their messages to a server. The server sends the messages to all clients in the order it received them; If message A was sent before message B, but the server received B first, it will broadcast message B to all clients first, then message A.

![consistent_ordering](https://github.com/user-attachments/assets/27f756ca-c83e-495e-b35e-59afc366a6b4)

# Casual Ordering
In casual ordering, the goal is for the chat to reflect on cause-and-effect (for example, a response appearing after a question). This is done by using timestamps to keep track of events. Because there’s the issue of each node having different clocks, this can’t be solved with a server or by comparing timestamps.

Thus, this is solved by having each node maintain a “vector clock” (vector or timestamps), with a slot for each node (itself included).  A node can track the events of all the nodes on its own clock. 
When a node sends a message, it increments its own counter in its vector clock, and sends the vector clock with the message. When the receiver sees the message, it compares the sent vector to its own to see if the event is related to any others.

The check_casual_order method ensures a received message meets the conditions of casual ordering:
- For the sending client’s position in the vector, it must be exactly one ahead of the receiving client’s vector.
- For all other clients, the sender’s clock values must be equal to or behind the receiving client’s vector.

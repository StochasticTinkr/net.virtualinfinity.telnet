# net.virtualinfinity.telnet
Low-level telnet library in Java using Non-Blocking NIO.

This is neither a Telnet server nor a Telnet client, but it provides enough of a framework
to build them.  The only thing you have to do is give it a SocketChannel.

This library uses NIO. In particular, it uses [net.virtualinfinity.nio](https://github.com/DanielPitts/net.virtualinfinity.nio)
which is a thin Event Queue wrapper around NIO select().

### Status

This code is ready to use, but it isn't complete. There are a lot of Telnet options which would
be useful to implement.

The code is also likely to evolve quite a bit before the archetecture stablizes.  In particular,
the "remote" vs "local" option handling needs to be better separated.

The set of built-in option handlers also needs to be expanded, but it is easy enough to supply
your own.


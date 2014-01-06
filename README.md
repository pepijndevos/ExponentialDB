# ExponentialDB

Pronunication: Mumble "exponential decay" so much that "decay" sounds like "DB"

## What

An in-memory database that uses persistent datastructures.

Old revisions of the database share structure witht the current revision, but are decayed to save memory.

Future versions might spill old data to disk using merge trees.

## Why

I love Redis, but I want to have nested datastructures and immutable data.

Since memory is more limited that disk space, exponential decay seemed like a nice solution.

## Usage

Provide a well-written Redis client with the json file of this database and enjoy.

Check said json file for available commands.

## License

Copyright © 2014 Pepijn de Vos

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

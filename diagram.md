```mermaid
flowchart TD;
    C[App] <-- TCP + Protocol Buffers --> G[Gateway]
    G <--TCP--> L[Lamp]
    G <--TCP--> A[Air Conditioner]
    G <--TCP--> SS[Stereo System]

    G <-- Group Communication --> Group
    L[Lamp] <-- Group Communication --> Group
    A[Air Conditioner] <-- Group Communication --> Group
    SS[Audio System] <-- Group Communication --> Group
```
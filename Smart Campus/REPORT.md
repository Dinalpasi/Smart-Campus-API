# Smart Campus API — Short Report

## JAX-RS Resource Lifecycle
In JAX-RS (and Jersey by default), resource classes are typically **per-request**: a new instance of a resource class is created for each incoming HTTP request. A resource only behaves like a singleton if it is explicitly registered/managed that way (for example, registering a single instance or using a DI container with singleton scope).

In this project, request-to-request state is stored in the in-memory maps (see `InMemoryStore`), not in resource object fields, so the observable API state persists regardless of whether Jersey uses per-request resource instances.

## Hypermedia / HATEOAS (Why it’s “Advanced”)
HATEOAS (Hypermedia as the Engine of Application State) is an advanced REST design feature because responses don’t just return data; they also include **links and available actions** that guide the client through the application dynamically (instead of the client hardcoding URL structures).

It’s “advanced” because it requires careful design of representations (link structure, relation types, consistency, versioning strategy), more complex testing, and often adoption of a hypermedia format (e.g., HAL, Siren, JSON:API). The payoff is **looser coupling** between client and server and safer evolvability of endpoints.

### Short HATEOAS example
Instead of returning only raw data/paths, a hypermedia response can embed links that a client can follow:

```json
{
  "version": "v1",
  "_links": {
    "self": { "href": "/api/v1/" },
    "rooms": { "href": "/api/v1/rooms" },
    "sensors": { "href": "/api/v1/sensors" }
  }
}
```

## Idempotency of DELETE Requests
An operation is **idempotent** if repeating it results in the same final server state.

`DELETE /rooms/{roomId}` is *conceptually idempotent*: once the room is deleted, further deletes do not “delete it more.” It’s common for APIs to return:
- First delete: `200 OK` or `204 No Content`
- Subsequent delete: `404 Not Found`

Even if the status code changes, the final state is still the same (room absent), so DELETE remains idempotent in the REST sense.

## Format Mismatch in POST Requests (Content-Type)
If a client sends a payload in the wrong format—e.g., `Content-Type: text/plain` to an endpoint that expects JSON (`@Consumes(application/json)`):
- The server may respond with **415 Unsupported Media Type** (or sometimes **400 Bad Request** if parsing fails differently).
- The resource method may **not execute**, because the framework rejects the request before invocation.
- The result is a failed operation: no resource is created/updated, and the client must fix headers and/or payload formatting.

Correct `Content-Type` headers are essential for reliable client/server integration.

## Query Parameters vs URL Path (Filtering Sensors)
### Query parameters (recommended for filtering)
Example: `GET /api/v1/sensors?type=temp`
- **Pros**: natural for optional filters; easy to extend (`?type=temp&status=ACTIVE`); avoids route explosion.
- **Cons**: slightly less “sub-collection-like” than a dedicated path.

### URL path elements (better for strict hierarchy)
Example: `GET /api/v1/sensors/type/temp`
- **Pros**: can read like a distinct sub-collection.
- **Cons**: awkward when multiple optional filters exist; more routes to maintain; harder to evolve as filtering grows.

For this project’s “filter by type if present” behavior, **query parameters are the clearer and more scalable choice**.


# React frontend

The existing SecuritySystemv2 interface, preserved during the Java backend migration.
Its pages serve parents, trusted persons, administrators, and security guards,
including student management and access-report downloads on this branch.
Use Node.js 22.12+ and npm. Requests use the real backend through
`src/services/api`; there is no mock API service.

```sh
npm ci
npm run build
npm run dev
```

`npm run build` creates the production bundle. `npm run dev` starts Vite at
[localhost:5173](http://localhost:5173), proxying `/api` requests to the Java backend
at `localhost:8080`.

See the [main README](../README.md) for Docker startup, environment variables,
database restoration, authentication, and testing instructions.

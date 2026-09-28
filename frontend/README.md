# Petify Frontend

The frontend application for **Petify**, an online platform for browsing, listing, and managing pets.

Petify provides a user-friendly interface where visitors can explore pet listings, view detailed information, save favorites, review owners, manage their profiles, and interact with platform features based on their assigned role.

This repository contains the client-side application built with **Vue 3**, **TypeScript**, and **Vite**.

## Features

### User features

- Create a new account
- Log in to an existing account
- Browse available pet listings
- View detailed information about a listing
- Add or remove listings from favorites
- View and update a user profile
- View owner profiles
- Submit and view reviews

### Owner and clinic features

- Manage pet-related information
- Access an owner profile
- Use a dedicated clinic dashboard
- Interact with listings and platform users

### Administrator features

- View and manage registered clients
- Review and manage pet listings
- Moderate platform content
- Access dedicated administration pages

## Technologies

- **Vue 3** – user interface framework
- **TypeScript** – static typing
- **Vite** – development and build tool
- **Vue Router** – client-side navigation
- **Pinia** – state management
- **Bootstrap 5** – responsive styling and UI components
- **Vue Datepicker** – date selection components
- **ESLint** – code quality
- **Prettier** – code formatting

## Related Repository

This repository contains only the frontend application.

The backend is available here:

[Petify Backend](https://github.com/veronika-ilioska/petify-backend)

## Project Structure

```text
petify-frontend/
├── public/
├── src/
│   ├── api/
│   │   ├── admin.ts
│   │   ├── auth.ts
│   │   ├── favorites.ts
│   │   ├── listings.ts
│   │   ├── profile.ts
│   │   └── reviews.ts
│   ├── components/
│   ├── data/
│   ├── img/
│   ├── router/
│   ├── stores/
│   ├── styles/
│   ├── types/
│   ├── views/
│   │   ├── AdminClientsView.vue
│   │   ├── AdminListingsView.vue
│   │   ├── AdminModerationView.vue
│   │   ├── ClinicDashboardView.vue
│   │   ├── ListingDetailsView.vue
│   │   ├── ListingsView.vue
│   │   ├── LoginView.vue
│   │   ├── OwnerProfileView.vue
│   │   ├── ProfileView.vue
│   │   └── SignupView.vue
│   ├── App.vue
│   └── main.ts
├── .env
├── package.json
├── tsconfig.json
└── vite.config.ts
```

## Prerequisites

Before running the project, make sure you have installed:

- **Node.js 20.19 or newer**
- **npm**
- The Petify backend application

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/veronika-ilioska/petify-frontend.git
```

### 2. Open the project directory

```bash
cd petify-frontend
```

### 3. Install dependencies

```bash
npm install
```

### 4. Configure the backend URL

Create or update the `.env` file in the root directory.

```env
VITE_API_BASE_URL=http://localhost:8080
```

Replace the URL with the address where the Petify backend is running.

Only environment variables beginning with `VITE_` are exposed to the Vue application.

### 5. Start the development server

```bash
npm run dev
```

The application will normally be available at:

```text
http://localhost:5173
```

If port `5173` is already in use, Vite may automatically select another available port.

## Available Scripts

### Start the development server

```bash
npm run dev
```

### Type-check and build for production

```bash
npm run build
```

### Preview the production build

```bash
npm run preview
```

### Run ESLint

```bash
npm run lint
```

### Format the source code

```bash
npm run format
```

### Run TypeScript type checking

```bash
npm run type-check
```

## Backend Connection

The frontend communicates with the Petify backend through API modules located in `src/api`.

These modules organize requests for:

- Authentication
- Listings
- Favorites
- Profiles
- Reviews
- Administrator operations

Make sure that:

1. The backend is running.
2. The frontend environment variable points to the correct backend URL.
3. The backend CORS configuration allows requests from the frontend origin.

For local development, the backend should normally allow:

```text
http://localhost:5173
```

For deployment, update the backend CORS configuration to allow the deployed frontend URL.

## Production Build

Create an optimized production build with:

```bash
npm run build
```

The generated files will be stored in:

```text
dist/
```

The `dist` directory can be deployed to platforms such as:

- Render
- Netlify
- Vercel
- GitHub Pages
- Any static web server

Because this application uses Vue Router, the hosting platform should redirect unknown routes to `index.html`.

## Academic Context

Petify was developed as a full-stack project for the **Databases** course.

## Author

**Veronika Ilioska**

GitHub: [veronika-ilioska](https://github.com/veronika-ilioska)

## License

This project was created for educational purposes.

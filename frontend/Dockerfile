# Use latest Node.js as base image
FROM node:latest

# Working directory in container
WORKDIR /app

# Copy package.json and package-lock.json (if they exist)
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the rest of the application
COPY . .

# Expose port 5173 for Vite
EXPOSE 5173

# Start the application in dev mode
# Vite needs --host to be accessible from outside the container
CMD ["npm", "run", "dev", "--", "--host"]

#!/bin/bash

# Temporal Workflow Bootcamp - GitBook Local Development Script

echo "🚀 Starting Temporal Workflow Bootcamp GitBook..."

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 18+ to continue."
    exit 1
fi

# Check Node.js version
NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 18 ]; then
    echo "⚠️ Node.js version 18+ recommended. Current version: $(node -v)"
fi

# Check if HonKit is installed globally
if ! command -v honkit &> /dev/null; then
    echo "📦 Installing HonKit globally..."
    npm install -g honkit
fi

# Install dependencies if needed
if [ ! -d "node_modules" ]; then
    echo "📦 Installing npm dependencies..."
    npm install
fi

# Build first to check for issues
echo "🔨 Building GitBook..."
if npx honkit build; then
    echo "✅ Build successful!"
else
    echo "❌ Build failed. Please check the configuration."
    exit 1
fi

# Serve the GitBook
echo "🌐 Starting local server..."
echo "📖 Your GitBook will be available at: http://localhost:4000"
echo "✨ Happy learning!"
echo "🔧 Press Ctrl+C to stop the server"

npx honkit serve 
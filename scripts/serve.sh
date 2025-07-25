#!/bin/bash

# Temporal Workflow Bootcamp - GitBook Local Development Script

echo "🚀 Starting Temporal Workflow Bootcamp GitBook..."

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 14+ to continue."
    exit 1
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

# Install GitBook plugins
echo "🔌 Installing GitBook plugins..."
honkit install

# Serve the GitBook
echo "🌐 Starting local server..."
echo "📖 Your GitBook will be available at: http://localhost:4000"
echo "✨ Happy learning!"

honkit serve 
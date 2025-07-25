# GitBook Setup and Deployment Guide

This document explains how the Temporal Workflow Bootcamp has been converted to a GitBook-style documentation site using HonKit and deployed via GitHub Pages.

## 📚 GitBook Structure

The repository has been configured as a GitBook with the following structure:

### Core Files
- `README.md` - Main landing page and course overview
- `SUMMARY.md` - Navigation structure for the entire course
- `book.json` - GitBook configuration (minimal, plugin-free setup)
- `package.json` - Node.js dependencies and build scripts

### Content Organization
- `class/Introduction.md` - Course introduction and motivation
- `class/temporal-architecture.md` - Architecture overview
- `class/intro-diagram.md` - Visual introduction
- `class/modules/lesson_*/` - 17 comprehensive lessons with concepts and workshops
- `class/workshop/` - Hands-on workshop guides and solutions

## 🛠 Local Development

### Prerequisites
- Node.js 18 or higher
- npm package manager

### Quick Start
```bash
# Method 1: Use the convenience script
./scripts/serve.sh

# Method 2: Manual setup
npm install -g honkit
npm install
honkit build
honkit serve
```

### Available Scripts
```bash
# Start local development server
npm run serve

# Build the static site
npm run build

# Generate PDF (requires calibre)
npm run pdf

# Generate EPUB
npm run epub

# Generate MOBI
npm run mobi
```

## 🚀 Deployment

### GitHub Pages (Automatic)
The repository is configured for automatic deployment to GitHub Pages:

1. **Workflow**: `.github/workflows/deploy.yml`
2. **Trigger**: Push to `main` branch
3. **Build**: Uses HonKit to generate static site
4. **Deploy**: Automatically publishes to GitHub Pages

### Manual Deployment
```bash
# Build the site
honkit build

# The generated site will be in _book/
# Upload the contents of _book/ to your hosting provider
```

## 📖 GitBook Features

### Simplified Configuration
The current setup uses a minimal configuration to ensure maximum compatibility:

- **No plugins**: Avoided plugin compatibility issues
- **Standard highlighting**: Uses basic syntax highlighting
- **Clean build**: No warnings or errors during generation
- **Fast performance**: Minimal overhead, quick builds

### Navigation Structure
The course is organized into logical sections:

1. **Introduction** - Welcome, architecture, and visual overview
2. **Core Modules** - 17 lessons from basics to advanced topics
3. **Workshop Guides** - Hands-on exercises and instructions
4. **Additional Resources** - Supporting documentation

## 🎯 Course Content

### Learning Path
- **Foundations (Lessons 1-4)**: Basic concepts and setup
- **Core Patterns (Lessons 5-9)**: Essential workflow patterns
- **Advanced Features (Lessons 10-13)**: Interactive and complex workflows
- **Production Readiness (Lessons 14-17)**: Deployment and operations

### Content Types
- **Concept**: Theoretical background and best practices
- **Workshop**: Hands-on coding exercises
- **Diagram**: Visual explanations and architecture
- **Solutions**: Complete implementations and answers

## 🔧 Customization

### Modifying Content
1. Edit Markdown files in the `class/` directory
2. Update `SUMMARY.md` if adding/removing pages
3. Commit changes to trigger automatic deployment

### Adding New Lessons
1. Create new lesson directory: `class/modules/lesson_X/`
2. Add lesson files: `concept.md`, `workshop_X.md`, `diagram.md`
3. Update `SUMMARY.md` to include new lesson
4. Add workshop guides if needed

### Adding Plugins (Advanced)
The current setup is plugin-free for maximum compatibility. If you want to add plugins:

1. Add them to `book.json` plugins array
2. Install plugin packages in GitHub Actions workflow
3. Test locally before deployment

## 🚨 Troubleshooting

### Common Issues We Resolved

#### 1. Plugin Compatibility Issues
**Problem**: Plugins like `edit-link`, `expandable-chapters-small` caused build failures
**Solution**: Use minimal configuration without plugins

#### 2. Mermaid/Log Language Warnings
**Problem**: HonKit highlight plugin doesn't recognize `mermaid` and `log` languages
**Solution**: Disable highlight plugin with `"-highlight"` in plugins array

#### 3. Node.js Version Compatibility
**Problem**: Some packages require Node.js 20+
**Solution**: Use Node.js 20 in GitHub Actions and recommend locally

### Debug Steps
```bash
# Check HonKit version
honkit --version

# Clean and rebuild
rm -rf _book node_modules
npm install
honkit build

# Test local server
honkit serve
```

### Build Status
```bash
# Successful build output should show:
info: X plugins are installed 
info: X explicitly listed 
info: found XX pages 
info: found XX asset files 
info: >> generation finished with success in X.Xs !
```

## 📊 Performance

### Optimizations
- **Minimal plugins**: Faster build times and fewer dependencies
- **Static HTML**: Fast loading with GitHub Pages CDN
- **Mobile responsive**: Works well on all devices
- **Search included**: Built-in search functionality

### Analytics
- GitHub Pages provides basic analytics
- Can add Google Analytics via custom plugins if needed

## 📞 Support

### Resources
- [HonKit Documentation](https://github.com/honkit/honkit)
- [GitHub Pages Guide](https://pages.github.com/)
- [Troubleshooting Guide](#troubleshooting)

### Contributing
1. Fork the repository
2. Make your changes
3. Test locally with `honkit build && honkit serve`
4. Submit a pull request

## 🎉 Success!

Your Temporal Workflow Bootcamp is now available as a professional GitBook documentation site! The configuration is:

✅ **Plugin-free and stable**  
✅ **Automatically deployed to GitHub Pages**  
✅ **Mobile responsive and fast**  
✅ **Includes 72 pages of comprehensive content**  
✅ **Built-in search functionality**  
✅ **Clean build with no errors or warnings**  

The site will be available at your GitHub Pages URL once deployed. The content is organized, searchable, and ready for learners worldwide.

**Happy Teaching! 📚✨** 
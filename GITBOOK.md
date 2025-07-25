# GitBook Setup and Deployment Guide

This document explains how the Temporal Workflow Bootcamp has been converted to a GitBook-style documentation site using HonKit and deployed via GitHub Pages.

## 📚 GitBook Structure

The repository has been configured as a GitBook with the following structure:

### Core Files
- `README.md` - Main landing page and course overview
- `SUMMARY.md` - Navigation structure for the entire course
- `book.json` - GitBook configuration with plugins and settings
- `package.json` - Node.js dependencies and build scripts

### Content Organization
- `class/Introduction.md` - Course introduction and motivation
- `class/temporal-architecture.md` - Architecture overview
- `class/intro-diagram.md` - Visual introduction
- `class/modules/lesson_*/` - 17 comprehensive lessons with concepts and workshops
- `class/workshop/` - Hands-on workshop guides and solutions

## 🛠 Local Development

### Prerequisites
- Node.js 14 or higher
- npm or yarn package manager

### Quick Start
```bash
# Method 1: Use the convenience script
./scripts/serve.sh

# Method 2: Manual setup
npm install -g honkit
npm install
honkit install
honkit serve
```

### Available Scripts
```bash
# Start local development server
npm run serve

# Build the static site
npm run build

# Install GitBook plugins
npm run install

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

### Enabled Plugins
- **github**: GitHub integration and links
- **edit-link**: Direct editing links to GitHub
- **prism**: Enhanced syntax highlighting
- **copy-code-button**: Copy code blocks with one click
- **expandable-chapters-small**: Collapsible navigation
- **anchors**: Anchor links for headings
- **back-to-top-button**: Quick navigation
- **theme-comscore**: Custom theme

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

### Styling
- Modify `book.json` to change plugins and configuration
- Custom CSS can be added via plugins
- Theme can be changed in the `pluginsConfig` section

## 📊 Analytics and Monitoring

### GitHub Pages Insights
- Access via GitHub repository settings
- Monitor page views and traffic
- Track popular content sections

### Performance
- GitBook generates static HTML for fast loading
- GitHub Pages provides global CDN
- Optimized for mobile and desktop viewing

## 🚨 Troubleshooting

### Common Issues
1. **Build failures**: Check Node.js version and dependencies
2. **Plugin errors**: Verify `book.json` configuration
3. **Deployment issues**: Check GitHub Actions logs
4. **Missing content**: Verify `SUMMARY.md` paths

### Debug Steps
```bash
# Check HonKit version
honkit --version

# Validate book.json
honkit build --debug

# Clean and rebuild
rm -rf _book node_modules
npm install
honkit install
honkit build
```

## 📞 Support

### Resources
- [HonKit Documentation](https://github.com/honkit/honkit)
- [GitBook Legacy Docs](https://docs.gitbook.com/v2-changes/important-differences)
- [GitHub Pages Guide](https://pages.github.com/)

### Contributing
1. Fork the repository
2. Make your changes
3. Test locally with `honkit serve`
4. Submit a pull request

---

## 🎉 Success!

Your Temporal Workflow Bootcamp is now available as a professional GitBook documentation site! The content is organized, searchable, and automatically deployed to GitHub Pages for easy access by learners worldwide.

**Happy Teaching! 📚✨** 
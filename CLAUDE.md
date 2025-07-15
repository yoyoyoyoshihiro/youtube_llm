# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

YouTube→NotebookLM PWA - A secure, privacy-focused Progressive Web App that enables users to easily summarize YouTube videos using NotebookLM.

## Technology Stack

- **Frontend**: HTML5, CSS3, Vanilla JavaScript (ES6+)
- **PWA**: Web App Manifest, Service Worker
- **Security**: Content Security Policy, HTTPS, Input validation
- **Architecture**: Client-side only, no server dependencies

## Key Files Structure

```
/production-pwa.html       - Main application (production-ready)
/manifest-secure.json      - PWA manifest with security features
/sw-secure.js             - Service Worker with security audit
/complete-user-guide.md   - Comprehensive user documentation
/pwa-demo.html           - Interactive demo for testing
/security-test-checklist.md - Security validation checklist
```

## Development Workflow

### Local Testing
```bash
# Serve files locally (Python)
python3 -m http.server 8000

# Or using Node.js
npx http-server -p 8000

# Access: http://localhost:8000/production-pwa.html
```

### Security Testing
- Run through `security-test-checklist.md`
- Test with various URL formats and edge cases
- Verify CSP headers and security policies
- Test offline functionality

### Deployment
- Host on HTTPS-enabled server
- Ensure proper MIME types for `.json` and `.js` files
- Configure security headers at server level
- Test PWA installation and share target functionality

## Architecture Principles

### Security-First Design
- All processing happens client-side
- No data storage or external transmission
- Strict input validation and sanitization
- CSP and security headers implementation

### Privacy Protection
- Zero data collection
- No cookies or tracking
- Automatic data cleanup
- Transparent processing

### User Experience
- 30-second installation
- Intuitive share menu integration
- Offline capability
- Responsive design

## Common Commands

### Testing PWA Features
```javascript
// Test share target in browser console
navigator.share({
  title: 'Test Video',
  text: 'https://youtube.com/watch?v=test123',
  url: 'https://youtube.com/watch?v=test123'
});

// Test service worker registration
navigator.serviceWorker.getRegistration()
  .then(reg => console.log('SW Status:', reg));
```

### Security Validation
```javascript
// Test URL validation
const testUrls = [
  'https://youtube.com/watch?v=abc123',
  'https://youtu.be/abc123',
  'https://malicious-site.com/fake'
];

// Manual validation testing in browser console
testUrls.forEach(url => {
  console.log(url, validateYouTubeURL(url));
});
```

## Important Security Notes

- **Input Validation**: Only YouTube URLs are accepted
- **CSP Policy**: Strict content security policy prevents XSS
- **Rate Limiting**: 10 requests per minute to prevent abuse
- **Error Handling**: Fail-safe mechanisms for all edge cases

## Compliance Requirements

- **GDPR**: No personal data collection
- **CCPA**: No data selling or sharing
- **YouTube ToS**: Legitimate URL sharing only
- **App Store Policies**: No policy violations

## Troubleshooting

### PWA Installation Issues
- Ensure HTTPS (required for PWA)
- Check manifest.json MIME type
- Verify service worker registration
- Test in supported browsers (Chrome 91+, Safari 14+)

### Share Target Not Working
- Confirm PWA is installed
- Check manifest share_target configuration
- Verify URL parameters are passed correctly
- Test on actual mobile device

### Security Violations
- Check browser console for CSP violations
- Review security logs in service worker
- Validate input against security patterns
- Monitor for suspicious activity patterns

## Best Practices

- Always test security features before deployment
- Regular security audits using built-in tools
- Keep dependencies minimal (currently zero)
- Follow progressive enhancement principles
- Maintain offline-first approach
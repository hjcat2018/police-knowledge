import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import DOMPurify from 'dompurify'
import 'highlight.js/styles/github-dark.css'

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  breaks: true,
  langPrefix: 'language-',
  highlight: function (str: string, lang: string): string {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs-code-block"><code class="hljs language-' + lang + '">' +
               hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
               '</code></pre>'
      } catch (err) {
        console.error('[Markdown] Highlight error:', err)
      }
    }
    return '<pre class="hljs-code-block"><code class="hljs">' + md.utils.escapeHtml(str) + '</code></pre>'
  }
})

md.renderer.rules.link_open = function (tokens: any[], idx: number, options: any, _env: any, self: any): string {
  const token = tokens[idx]
  const hrefIndex = token.attrIndex('href')

  if (hrefIndex >= 0) {
    const href = token.attrs[hrefIndex][1]
    if (href && (href.startsWith('http://') || href.startsWith('https://'))) {
      token.attrPush(['target', '_blank'])
      token.attrPush(['rel', 'noopener noreferrer'])
    }
  }

  return self.renderToken(tokens, idx, options)
}

md.renderer.rules.image = function (tokens: any[], idx: number, options: any, _env: any, self: any): string {
  const token = tokens[idx]
  const src = token.attrGet('src')
  const alt = token.attrGet('alt') || '图片'

  if (src) {
    return `<div class="markdown-image-container">
              <img class="markdown-image" src="${src}" alt="${alt}" />
              ${alt ? `<span class="markdown-image-alt">${alt}</span>` : ''}
            </div>`
  }

  return self.renderToken(tokens, idx, options)
}

export function renderMarkdown(content: string): string {
  if (!content) return ''

  try {
    let html = md.render(content)
    html = DOMPurify.sanitize(html, {
      ADD_TAGS: ['iframe'],
      ADD_ATTR: ['allow', 'allowfullscreen', 'frameborder', 'scrolling']
    })
    return html
  } catch (e) {
    console.error('[Markdown] Render error:', e)
    return DOMPurify.sanitize(content.replace(/\n/g, '<br>'))
  }
}

export function renderMarkdownSync(content: string): string {
  if (!content) return ''

  try {
    let html = md.render(content)
    html = DOMPurify.sanitize(html, {
      ADD_TAGS: ['iframe'],
      ADD_ATTR: ['allow', 'allowfullscreen', 'frameborder', 'scrolling']
    })
    return html
  } catch (e) {
    console.error('[Markdown] Render error:', e)
    return DOMPurify.sanitize(content)
  }
}

export default {
  render: renderMarkdown,
  renderSync: renderMarkdownSync,
  md
}

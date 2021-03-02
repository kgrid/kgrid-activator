module.exports = {
  base: '/kgrid-activator/',
  title: 'KGrid Activator',
  themeConfig: {
    repo: 'kgrid/kgrid-activator',
    lastUpdated: 'Last Updated',
    nav: [
      { text: 'Guide', link: '/' },
      { text: 'Configuration', link: '/configuration'},
      { text: 'API', link: '/api'},
      { text: 'Containers', link: '/containers' }

    ],
    search: true,
    searchMaxSuggestions: 10,
    sidebar: 'auto',
    displayAllHeaders: true
  }
}

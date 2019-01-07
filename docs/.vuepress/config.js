module.exports = {
  base: '/kgrid-activator/',
  title: 'KGrid Activator',
  themeConfig: {
    repo: 'kgrid/kgrid-activator',
    lastUpdated: 'Last Updated',
    nav: [
      { text: 'Guide', link: '/' },
      { text: 'Configuration', link: '/configuration/'},
      { text: 'Adding KOs', link: '/kos/' },
      { text: 'Service Descriptions', link: '/service/' }
      { text: 'Docker', link: '/docker/' }

    ],
    search: true,
    searchMaxSuggestions: 10,
    sidebar: 'auto',
    displayAllHeaders: true
  }
}

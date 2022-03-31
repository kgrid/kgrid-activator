(window.webpackJsonp=window.webpackJsonp||[]).push([[5],{171:function(t,e,a){"use strict";a.r(e);var n=a(0),s=Object(n.a)({},(function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"content"},[t._m(0),t._v(" "),a("h2",{attrs:{id:"pull-from-dockerhub"}},[a("a",{staticClass:"header-anchor",attrs:{href:"#pull-from-dockerhub"}},[t._v("#")]),t._v(" "),a("a",{attrs:{href:"https://docs.docker.com/engine/reference/commandline/pull/",target:"_blank",rel:"noopener noreferrer"}},[t._v("Pull from DockerHub"),a("OutboundLink")],1)]),t._v(" "),t._m(1),a("h2",{attrs:{id:"running-the-image"}},[a("a",{staticClass:"header-anchor",attrs:{href:"#running-the-image"}},[t._v("#")]),t._v(" "),a("a",{attrs:{href:"https://docs.docker.com/engine/reference/commandline/run",target:"_blank",rel:"noopener noreferrer"}},[t._v("Running the Image"),a("OutboundLink")],1)]),t._v(" "),t._m(2),t._v(" "),t._m(3),a("ul",[a("li",[a("a",{attrs:{href:"https://docs.docker.com/engine/reference/commandline/run/#mount-volume--v---read-only",target:"_blank",rel:"noopener noreferrer"}},[t._v("Mapped to a local shelf"),a("OutboundLink")],1)])]),t._v(" "),t._m(4),t._m(5),t._v(" "),t._m(6),a("ul",[a("li",[a("p",[t._v("This example has a few things going on:")]),t._v(" "),a("ul",[a("li",[a("code",[t._v("--network host")]),t._v(" "),a("a",{attrs:{href:"https://docs.docker.com/engine/reference/commandline/run/#connect-a-container-to-a-network---network",target:"_blank",rel:"noopener noreferrer"}},[t._v("Running with a network bridge"),a("OutboundLink")],1),t._v(" (if your containerized activator needs to talk to the network, i.e. you're running an external runtime in another container)")]),t._v(" "),a("li",[a("code",[t._v("-it --rm")]),t._v(" Running interactive and Removing the Container when stopped. can be found in the "),a("a",{attrs:{href:"https://docs.docker.com/engine/reference/commandline/run/#options",target:"_blank",rel:"noopener noreferrer"}},[t._v("options"),a("OutboundLink")],1)]),t._v(" "),a("li",[a("code",[t._v("-e")]),t._v(" "),a("a",{attrs:{href:"https://docs.docker.com/engine/reference/commandline/run/#set-environment-variables--e---env---env-file",target:"_blank",rel:"noopener noreferrer"}},[t._v("Pass Environment Variables"),a("OutboundLink")],1)])])]),t._v(" "),t._m(7)]),t._v(" "),t._m(8),t._v(" "),t._m(9),t._v(" "),t._m(10),t._m(11),t._v(" "),a("p",[t._v("Then:")]),t._v(" "),t._m(12),t._v(" "),t._m(13),t._v(" "),t._m(14)])}),[function(){var t=this.$createElement,e=this._self._c||t;return e("h1",{attrs:{id:"kgrid-docker-containers"}},[e("a",{staticClass:"header-anchor",attrs:{href:"#kgrid-docker-containers"}},[this._v("#")]),this._v(" KGrid Docker Containers")])},function(){var t=this.$createElement,e=this._self._c||t;return e("div",{staticClass:"language-bash extra-class"},[e("pre",{pre:!0,attrs:{class:"language-bash"}},[e("code",[e("span",{pre:!0,attrs:{class:"token function"}},[this._v("docker")]),this._v(" pull kgrid/kgrid-activator\n")])])])},function(){var t=this.$createElement,e=this._self._c||t;return e("ul",[e("li",[this._v("Running in a container mapped to port 8080 (default port for the activator)")])])},function(){var t=this.$createElement,e=this._self._c||t;return e("div",{staticClass:"language-bash extra-class"},[e("pre",{pre:!0,attrs:{class:"language-bash"}},[e("code",[this._v("  "),e("span",{pre:!0,attrs:{class:"token function"}},[this._v("docker")]),this._v(" run -p "),e("span",{pre:!0,attrs:{class:"token number"}},[this._v("8080")]),this._v(":8080 --name activator kgrid/kgrid-activator\n")])])])},function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"language-bash extra-class"},[a("pre",{pre:!0,attrs:{class:"language-bash"}},[a("code",[t._v("  "),a("span",{pre:!0,attrs:{class:"token function"}},[t._v("docker")]),t._v(" run -p "),a("span",{pre:!0,attrs:{class:"token number"}},[t._v("8080")]),t._v(":8080 -v "),a("span",{pre:!0,attrs:{class:"token variable"}},[t._v("${"),a("span",{pre:!0,attrs:{class:"token environment constant"}},[t._v("PWD")]),t._v("}")]),t._v("/shelf:/applications/shelf --name activator -d kgrid/kgrid-activator \n")])])])},function(){var t=this.$createElement,e=this._self._c||t;return e("ul",[e("li",[this._v("Example:")])])},function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"language-bash extra-class"},[a("pre",{pre:!0,attrs:{class:"language-bash"}},[a("code",[t._v("  "),a("span",{pre:!0,attrs:{class:"token function"}},[t._v("docker")]),t._v(" run -it --rm --network "),a("span",{pre:!0,attrs:{class:"token function"}},[t._v("host")]),t._v(" -p "),a("span",{pre:!0,attrs:{class:"token number"}},[t._v("8080")]),t._v(":8080 -e "),a("span",{pre:!0,attrs:{class:"token assign-left variable"}},[t._v("SPRING_PROFILES_ACTIVE")]),a("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v("dev -v "),a("span",{pre:!0,attrs:{class:"token variable"}},[t._v("${"),a("span",{pre:!0,attrs:{class:"token environment constant"}},[t._v("PWD")]),t._v("}")]),t._v("/shelf:/application/shelf --name activator kgrid/kgrid-activator:latest\n")])])])},function(){var t=this.$createElement,e=this._self._c||t;return e("li",[e("p",[this._v("Once created, you can stop and start the container using "),e("code",[this._v("docker stop activator")]),this._v(" and "),e("code",[this._v("docker start acivator")]),this._v(".")])])},function(){var t=this.$createElement,e=this._self._c||t;return e("h2",{attrs:{id:"quick-start-with-docker-compose"}},[e("a",{staticClass:"header-anchor",attrs:{href:"#quick-start-with-docker-compose"}},[this._v("#")]),this._v(" Quick start with "),e("code",[this._v("docker-compose")])])},function(){var t=this.$createElement,e=this._self._c||t;return e("p",[this._v("You can also start the activator in your environment by setting up "),e("code",[this._v("docker-compose.yaml")]),this._v(" file, shown below as an example")])},function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"language-yaml extra-class"},[a("pre",{pre:!0,attrs:{class:"language-yaml"}},[a("code",[a("span",{pre:!0,attrs:{class:"token key atrule"}},[t._v("version")]),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(":")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token string"}},[t._v('"3.6"')]),t._v("\n\n"),a("span",{pre:!0,attrs:{class:"token key atrule"}},[t._v("services")]),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(":")]),t._v("\n  "),a("span",{pre:!0,attrs:{class:"token key atrule"}},[t._v("activator")]),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(":")]),t._v("\n    "),a("span",{pre:!0,attrs:{class:"token key atrule"}},[t._v("container_name")]),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(":")]),t._v(" lion"),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("-")]),t._v("activator\n    "),a("span",{pre:!0,attrs:{class:"token key atrule"}},[t._v("environment")]),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(":")]),t._v("\n        "),a("span",{pre:!0,attrs:{class:"token key atrule"}},[t._v("KGRID_CONFIG")]),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(":")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token string"}},[t._v('"--kgrid.shelf.cdostore.url=filesystem:file://shelf --cors.url=*  --management.info.git.mode=full"')]),t._v("\n    "),a("span",{pre:!0,attrs:{class:"token key atrule"}},[t._v("image")]),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(":")]),t._v(" kgrid/activator"),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(":")]),t._v("1.5.2\n    "),a("span",{pre:!0,attrs:{class:"token key atrule"}},[t._v("ports")]),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(":")]),t._v("\n      "),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("-")]),t._v(" 8080"),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(":")]),a("span",{pre:!0,attrs:{class:"token number"}},[t._v("8080")]),t._v("\n    "),a("span",{pre:!0,attrs:{class:"token key atrule"}},[t._v("volumes")]),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(":")]),t._v("\n      "),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("-")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token string"}},[t._v('"activator_shelf:/home/kgrid/shelf"')]),t._v("\n\n"),a("span",{pre:!0,attrs:{class:"token key atrule"}},[t._v("volumes")]),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(":")]),t._v("\n  "),a("span",{pre:!0,attrs:{class:"token key atrule"}},[t._v("activator_shelf")]),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(":")]),t._v("\n")])])])},function(){var t=this.$createElement,e=this._self._c||t;return e("p",[this._v("that uses the the "),e("code",[this._v("kgrid/activator:1.5.2")]),this._v(" image, with presets for port and shelf.")])},function(){var t=this.$createElement,e=this._self._c||t;return e("p",[e("code",[this._v("docker-compose up")])])},function(){var t=this.$createElement,e=this._self._c||t;return e("h4",{attrs:{id:"good-to-know"}},[e("a",{staticClass:"header-anchor",attrs:{href:"#good-to-know"}},[this._v("#")]),this._v(" Good to Know")])},function(){var t=this.$createElement,e=this._self._c||t;return e("ol",[e("li",[this._v("View Container Logs  "),e("code",[this._v("docker logs activator")])]),this._v(" "),e("li",[this._v("Start a shell in the container "),e("code",[this._v("docker exec -it activator sh")])])])}],!1,null,null,null);e.default=s.exports}}]);
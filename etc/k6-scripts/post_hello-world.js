import http from 'k6/http'
import { check } from 'k6'

// const url = "http://localhost:8080/hello/world/v0.0.1/welcome"
// const url = "https://kgrid-activator.herokuapp.com/99999/newko/v0.0.1/welcome"
// const url = "http://e367381a.ngrok.io/hello/world/v0.0.1/welcome"

let url =
  getFromEnv(__ENV.baseUrl, 'http://localhost:8080') +
  getFromEnv(__ENV.endpoint, '/hello/world/v0.0.1/welcome')

const params = {headers: {'content-type': 'application/json'}}
const body = JSON.stringify({name: 'Bob'})

export function setup () {
  console.log('Checking connection: ')

  console.log('url: ' + url)
  let response = http.post(
    url,
    body,
    params
  )
  console.log('response: ' + response.body) // run once to check and display

  if (response.status !== 200) {
    throw new Error('Initial connection failed')
  }
}

export default function () {
  let result = http.post(
    url,
    body,
    params
  )
  check(result, {
    'is status 200': (r) => r.status === 200
  })

};

//can be passed in with '--env xxx=value',
// and accessed with getEnvVariable('__ENV.xxx', defaultValue)
function getFromEnv (variable, defaultValue) {
  return variable === undefined ? defaultValue : variable
}

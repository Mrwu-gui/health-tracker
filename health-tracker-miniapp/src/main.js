import { createSSRApp } from "vue";
import App from "./App.vue";

export function createApp() {
	console.log(2222222)
  const app = createSSRApp(App);
  return { app };
}

// API配置文件
// 本地开发时设置为 true，打包上线前改为 false
const IS_DEV = false;

const DEV_BASE_URL = "http://127.0.0.1:8080";
const PROD_BASE_URL = "https://datewell.xyz";

export const API_BASE_URL = IS_DEV ? DEV_BASE_URL : PROD_BASE_URL;

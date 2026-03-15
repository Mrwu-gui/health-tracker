<script>
import { ensureDevLogin } from "./utils/dev-auth";
export default {
  onLaunch() {
    ensureDevLogin();
    if (typeof wx !== "undefined" && wx.cloud) {
      wx.cloud.init({
        env: "health-4gt7erwb330dae07"
      });
    }
  },
  onShow() {},
  onHide() {}
};
</script>

<style>
/* global styles are defined in pages */
</style>

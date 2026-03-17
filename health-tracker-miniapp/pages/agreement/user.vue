<template>
  <view class="page">
    <view class="doc">
      <text class="doc-title">智康AI用户协议</text>
      <text class="doc-updated">更新日期：2026年3月</text>

      <text class="section-title">一、协议的接受与变更</text>
      <text class="body">
        欢迎使用智康AI（以下简称「本产品」）。使用本产品即表示您已阅读、理解并同意受本协议约束。我们可能适时修订本协议，修订后的协议将在产品内公布；若您继续使用本产品，即视为接受修订后的协议。
      </text>

      <text class="section-title">二、服务说明</text>
      <text class="body">
        本产品提供健康数据记录、目标管理、用药与提醒、家庭成员协作及 智康健康分析等相关功能。我们努力保证服务的可用性与准确性，但健康建议仅供参考，不能替代专业医疗诊断与治疗，如有健康问题请及时就医。
      </text>

      <text class="section-title">三、账号与安全</text>
      <text class="body">
        您应使用本人微信或其他我们支持的登录方式注册/登录账号，并妥善保管账号信息。因您自身原因导致账号泄露、被盗等造成的损失，需由您自行承担。如发现账号异常，请及时联系客服。
      </text>

      <text class="section-title">四、用户行为规范</text>
      <text class="body">
        使用本产品时，您应遵守法律法规及公序良俗，不得利用本产品从事违法违规活动、传播违法或不良信息、干扰或破坏服务正常运行，或侵害他人合法权益。我们有权对违规行为进行处理，包括但不限于限制功能、封禁账号等。
      </text>

      <text class="section-title">五、知识产权</text>
      <text class="body">
        本产品内的界面、文案、图标、逻辑等除另有说明外，其知识产权归我们或相关权利人所有。未经授权，您不得复制、修改、传播或用于商业目的。
      </text>

      <text class="section-title">六、免责与责任限制</text>
      <text class="body">
        在法律允许的范围内，本产品按「现状」提供，我们不对服务的及时性、准确性、完整性做任何明示或默示保证。因不可抗力、网络故障、第三方原因等导致的服务中断或数据异常，我们将尽力恢复但不承担相应责任。我们对您使用本产品所产生的直接或间接损失不承担责任，法律另有规定的除外。
      </text>

      <text class="section-title">七、协议终止</text>
      <text class="body">
        您可随时停止使用本产品；我们也可在合理必要情形下暂停或终止向您提供全部或部分服务。终止后，您使用服务的权利即行停止，我们可按隐私政策与法律规定处理您的数据。
      </text>

      <text class="section-title">八、其他</text>
      <text class="body">
        本协议适用中华人民共和国法律。本协议条款若部分无效，不影响其余条款效力。如有争议，双方应友好协商；协商不成的，可向我们住所地有管辖权的人民法院提起诉讼。
      </text>

      <text class="body end">感谢您选择智康AI。</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {};
  }
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 16px 20px 32px;
  padding-top: calc(16px + env(safe-area-inset-top));
  background: #fefcf9;
  color: #0f172a;
}

.doc {
  background: #fff;
  border-radius: 16px;
  padding: 20px 18px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.doc-title {
  font-size: 18px;
  font-weight: 700;
  text-align: center;
  margin-bottom: 4px;
}

.doc-updated {
  font-size: 12px;
  color: #64748b;
  text-align: center;
  margin-bottom: 8px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin-top: 8px;
}

.body {
  font-size: 13px;
  color: #475569;
  line-height: 1.65;
  display: block;
}

.body.end {
  margin-top: 8px;
  text-align: center;
  color: #f97316;
}
</style>

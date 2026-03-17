<template>
  <view class="page">
    <view class="doc">
      <text class="doc-title">智康AI隐私政策</text>
      <text class="doc-updated">更新日期：2026年3月</text>

      <text class="body intro">
        智康AI（以下简称「我们」）重视您的隐私。本政策说明我们如何收集、使用、存储与保护您的个人信息，以及您享有的权利。请您在使用本产品前仔细阅读。
      </text>

      <text class="section-title">一、我们收集的信息</text>
      <text class="body">
        <text class="bold">1. 账号与身份信息：</text>为完成登录与账号管理，我们会获取您通过微信等渠道提供的昵称、头像、唯一标识等（以实际授权范围为准）。\n
        <text class="bold">2. 健康与身体数据：</text>您主动填写的体重、身高、血压、心率、运动、睡眠、用药、目标、提醒等数据，用于为您提供记录、统计与 智康分析等服务。\n
        <text class="bold">3. 家庭成员相关：</text>若您使用家庭成员功能，可能涉及您填写的家人昵称、关系及您选择共享的数据范围。\n
        <text class="bold">4. 设备与日志信息：</text>为保障服务安全与稳定，我们可能收集设备型号、系统版本、唯一设备标识、操作日志等，用于排查问题与改进产品。
      </text>

      <text class="section-title">二、信息的使用目的</text>
      <text class="body">
        我们使用上述信息用于：向您提供健康记录、目标与提醒、家人协作及 智康健康分析等功能；保障账号与数据安全；改进产品体验与服务质量；在经您同意或法律允许的情况下进行必要的统计分析或合规处理。
      </text>

      <text class="section-title">三、信息的存储与保护</text>
      <text class="body">
        您的数据将存储在中华人民共和国境内。我们采用合理的技术与管理措施保护您的个人信息，防止未经授权的访问、使用或泄露。我们仅在实际业务与法律要求的必要期限内保留您的信息。
      </text>

      <text class="section-title">四、信息的共享、转让与公开</text>
      <text class="body">
        我们不会将您的个人信息出售给第三方。在以下情形我们可能共享或披露信息：经您同意；为完成合并、分立、收购等而转移；向委托我们处理数据的合作方共享（受严格约束）；根据法律法规或政府要求；为维护我们或用户的合法权益所必需。若涉及跨境提供，我们会依法履行相应义务。
      </text>

      <text class="section-title">五、您的权利</text>
      <text class="body">
        您有权查询、更正、删除您的个人信息，也可通过产品内「授权与隐私」等设置管理部分权限。您可联系我们申请注销账号或导出数据，我们将在合理期限内处理。撤回同意可能导致部分功能无法使用，但不影响此前基于同意所做的处理。
      </text>

      <text class="section-title">六、未成年人保护</text>
      <text class="body">
        若您为未成年人，请在监护人陪同下阅读本政策，并在监护人同意后使用本产品。我们不会主动收集未成年人的个人信息，若发现误收集将尽快删除。
      </text>

      <text class="section-title">七、政策更新与联系我们</text>
      <text class="body">
        我们可能适时修订本政策，修订版本将在产品内公布。若您继续使用本产品，即视为接受更新后的政策。如对本政策或您的个人信息处理有疑问，请通过产品内反馈或我们公示的联系方式与我们联系。
      </text>

      <text class="body end">感谢您信任智康AI。</text>
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

.body.intro {
  font-size: 13px;
  color: #475569;
  line-height: 1.65;
  margin-bottom: 4px;
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
  white-space: pre-line;
}

.bold {
  font-weight: 600;
  color: #334155;
}

.body.end {
  margin-top: 8px;
  text-align: center;
  color: #f97316;
  white-space: normal;
}
</style>

<template>
  <div class="seckill-list">
    <h2>秒杀活动</h2>
    <el-row :gutter="20">
      <el-col :span="6" v-for="product in seckillProducts" :key="product.id">
        <el-card :body-style="{ padding: '10px' }" class="product-card">
          <!-- <img v-lazy="product.img" class="product-image" /> -->
          <div class="product-info">
            <h3 class="product-name">{{ product.name }}</h3>
            <p>秒杀价：<span class="seckill-price">￥{{ product.seckillPrice }}</span></p>
            <p>库存：<span>{{ adjustedStockCount(product.stockCount) }}</span></p>
            <p>倒计时：<span>{{ getCountdown(product.startDate, product.endDate) }}</span></p>
            <el-button type="danger"
                       :disabled="!isSeckillAvailable(product.startDate, product.endDate) || buttonLoading[product.id]"
                       :loading="buttonLoading[product.goodsId]" @click="startVerify(product.goodsId)">
              {{ isSeckillAvailable(product.startDate, product.endDate) ? '立即抢购' : '不可抢购' }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-button type="default" @click="goToProducts">返回商品列表</el-button>
    <div v-if="seckillProducts.length === 0">
      <p>当前没有进行中的秒杀活动。</p>
    </div>
      <el-dialog title="请输入验证码" :visible.sync="captchaVisible" width="30%">
        <form @submit.prevent="handlePurchase()">
            <div>
                <input type="text" v-model="verifyCode" name="verifyCode" placeholder="请输入验证码" required="true">
                <img
                    alt="单击图片刷新！"
                    class="pointer"
                    :src="kaptchaSrc"
                    @click="refreshKaptcha"
                />
            </div>
            <span slot="footer" class="dialog-footer">
                <el-button @click="handleCancel()">取消</el-button>
                <el-button native-type="submit" type="primary">确认</el-button>
            </span>
        </form>
        <div v-if="showMessage">{{ message }}</div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'SeckillList',
  data() {
    return {
      seckillProducts: [], // 秒杀商品数据
      buttonLoading: {}, // 按钮加载状态
      captchaVisible: false, // 验证码弹窗是否可见
      verifyCode: '', // 用户输入的验证码
      kaptchaSrc: '/api/common/kaptcha', // 验证码图片地址
      showMessage: false, // 是否显示消息
      message: '' // 消息内容
    };
  },
  mounted() {
    this.fetchSeckillProducts();
    this.startCountdownUpdate();
  },
  beforeDestroy() {
    clearInterval(this.countdownTimer);
  },
  computed: {
    adjustedStockCount() {
      return (stockCount) => {
        // 检查库存是否小于 0，如果小于 0 则返回 0，否则返回实际库存
        return stockCount < 0? 0 : stockCount;
      };
    }
  },
  methods: {
    // 获取秒杀商品数据
    fetchSeckillProducts() {
      this.$axios
          .get('/api/user/seckill/secGoods')
          .then((response) => {
            if (response.status === 200) {
              this.seckillProducts = response.data;
            } else {
              this.$message.error(response.statusText);
            }
          })
          .catch((error) => {
            console.error(error);
            this.$message.error('获取秒杀商品列表失败');
          });
    },
    // 获取倒计时
    getCountdown(startDate, endDate) {
      startDate = this.convertToDate(startDate);
      endDate = this.convertToDate(endDate);
      const now = new Date().getTime();
      const startDiff = startDate - now;
      const endDiff = endDate - now;

      if (startDiff > 0) {
        return this.formatTime(startDiff); // 未开始
      } else if (endDiff > 0) {
        return '秒杀进行中'; // 已开始
      } else {
        return '活动已结束'; // 已结束
      }
    },
    // 格式化时间
    formatTime(ms) {
      const seconds = Math.floor(ms / 1000) % 60;
      const minutes = Math.floor(ms / (1000 * 60)) % 60;
      const hours = Math.floor(ms / (1000 * 60 * 60)) % 24;
      const days = Math.floor(ms / (1000 * 60 * 60 * 24));
      return `${days > 0 ? days + '天 ' : ''}${hours}小时 ${minutes}分钟 ${seconds}秒`;
    },
    // 判断秒杀是否可用
    isSeckillAvailable(startDate, endDate) {
      startDate = this.convertToDate(startDate);
      endDate = this.convertToDate(endDate);
      const now = new Date().getTime();
      return now >= startDate && now <= endDate;
    },

    // 将后端传来的日期数组转换为 JavaScript 可解析的日期格式
    convertToDate(dateArray) {
      // 假设 dateArray 是一个 [year, month, day, hour, minute] 格式
      // 注意：JavaScript 中的月份是 0-11 所以需要减 1
      const [year, month, day, hour, minute] = dateArray;
      return new Date(year, month - 1, day, hour, minute).getTime();
    },
    // 更新倒计时定时器
    startCountdownUpdate() {
      this.countdownTimer = setInterval(() => {
        this.seckillProducts = [...this.seckillProducts]; // 触发界面重新渲染
      }, 1000); // 每秒更新一次
    },
    refreshKaptcha() {
      this.kaptchaSrc = '/api/common/kaptcha?d=' + new Date().getTime();
    },
    startVerify(productId){
      if (this.buttonLoading[productId]) return;
      this.$set(this.buttonLoading, productId, true); // 设置按钮加载状态
      this.productId = productId;
      this.refreshKaptcha();
      this.captchaVisible = true; // 显示验证码弹窗
    },
    // 立即抢购操作
    handlePurchase() {
      // 防止重复点击
      console.log("购买秒杀商品id= ", this.productId)
      console.log('用户输入的验证码:', this.verifyCode);
  // 发送请求验证验证码
      if(this.verifyCode != "")
        this.$axios.post('/api/verify?verifyCode='+this.verifyCode)
            .then(response => {
              if (response.status === 200) {
                try {
                    this.doPurchase(`/api/user/seckill/sec?goodsId=${this.productId}`);
                } catch (purchaseError) {
                }
              } else {
                this.showMessage = true;
                this.message = '验证码错误，请重新输入';
                this.refreshKaptcha();
              }
            })
            .catch(error => {
              console.error('验证码验证请求错误:', error);
              this.showMessage = true;
              this.message = '验证码错误，请重新输入';
              this.refreshKaptcha();
            })

    },
    doPurchase(url){
      this.$axios
          .get(url) // 使用 GET 请求
          .then((response) => {
            if (response.status === 200) {
              this.$message.success(`秒杀商品${productId}成功`);
              this.fetchSeckillProducts(); // 更新商品列表
            } else if (response.status === 400) {
              this.$message.error('商品的库存量没有剩余或商品不存在,秒杀结束');
            } else if (response.status === 404) {
              this.$message.error('商品不存在');
            } else {
              this.$message.error(response.statusText);
            }
          })
          .catch((error) => {
            console.error(error);
            this.$message.error(error.response.data);
          })
          .finally(() => {
            this.$set(this.buttonLoading, productId, false); // 恢复按钮状态
          });
    },
    handleCancel() {
            // 取消操作，恢复按钮状态
            this.$set(this.buttonLoading, this.productId, false);
            this.captchaVisible = false;
    },
    goToProducts() {
            // 使用 $router.push 跳转到 /products 页面
            this.$router.push("/products");
    },
  },
};
</script>

<style scoped>
.seckill-list {
  padding: 20px;
}

.product-card {
  cursor: pointer;
  margin-bottom: 20px;
}

.product-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info {
  text-align: center;
}

.product-name {
  font-size: 16px;
  margin: 10px 0;
}

.seckill-price {
  color: red;
  font-size: 18px;
}
</style>
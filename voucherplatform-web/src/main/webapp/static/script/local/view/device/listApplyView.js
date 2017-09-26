var rootVue;
require_js_file(['vueValidator', 'vuePicker'], function (Vue, fnr, validator) {
    Vue.use(validator);
    fnr.dataPageTable('data-table-dyn', {
        audit: function (id) {
            var self = this;
            var params = {
                title: '终端审核（选择终端组）',
<<<<<<< HEAD
                url: '/device/auditDevice.html?id=' + id,
=======
                url: '/voucher/device/auditDevice.html?id=' + id,
>>>>>>> fix_master
                width: 1200,
                height: 800,
                callbackSucc: function () {
                    self.$parent.reload();
                }
            };
            fnr.iDialog(params);
        },
        auditFaild: function (id) {
            var self = this;
            var param = {};
            param.applyId = id;
<<<<<<< HEAD
            var defrend = fnr.ajaxJson("../api/device/deviceAudit", param);
=======
            var defrend = fnr.ajaxJson("/voucher/api/device/deviceAudit", param);
>>>>>>> fix_master
            defrend.then(function (result) {
                var resp = result.json();
                if (resp.code == 200) {
                    self.$parent.reload();
                }
            });
        }
    });

    rootVue = new Vue({
        el: '[vtag=root]',
        data: {
            dtSetting: {
                remote: {
                    link: function (params, options) {
                        console.log(JSON.stringify(params));
<<<<<<< HEAD
                        return fnr.ajaxJson("../api/device/selectApplyList", params, options);
=======
                        return fnr.ajaxJson("/voucher/api/device/selectApplyList", params, options);
>>>>>>> fix_master
                    },
                    options: {method: 'GET'},
                    isLoadOnPageInit: true
                }
            },
            queryFormData: {
                status: null,
                code: null,
            },
        },
        ready: function () {
        }
        ,
        methods: {
            queryFormSubmit: function () {
                this.$refs.dtList.query();
            },
            reload: function () {
                this.$refs.dtList.query();
            }
        }
    })
    ;
});

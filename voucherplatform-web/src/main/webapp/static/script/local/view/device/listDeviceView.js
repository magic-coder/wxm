var rootVue;
require_js_file(['vueValidator', 'vuePicker'], function (Vue, fnr, validator) {
    Vue.use(validator);
    fnr.dataPageTable('data-table-dyn', {
        getSignType: function (signType) {
            return this.$parent.signType[signType].toUpperCase();
        },
        delDevice: function (code) {
            var self = this;
            var param = {};
            param.code = code;
<<<<<<< HEAD
            var defrend = fnr.ajaxJson("../api/device/delDevice", param, {method: "GET"});
=======
            var defrend = fnr.ajaxJson("/voucher/api/device/delDevice", param, {method: "GET"});
>>>>>>> fix_master
            defrend.then(function (result) {
                var resp = result.json();
                if (resp.code == 200) {
                    self.reload();
                }
            });
        },
        rename: function (id, name) {
            var self = this;
            var params = {
                title: '设备名重命名',
<<<<<<< HEAD
                url: '/device/renameDevice.html?id=' + id + "&name=" + encodeURI(name),
=======
                url: '/voucher/device/renameDevice.html?id=' + id + "&name=" + encodeURI(name),
>>>>>>> fix_master
                width: 800,
                height: 400,
                callbackSucc: function () {
                    self.$parent.reload();
                }
            };
            fnr.iDialog(params);
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
                        return fnr.ajaxJson("../api/device/selectDeviceList", params, options);
=======
                        return fnr.ajaxJson("/voucher/api/device/selectDeviceList", params, options);
>>>>>>> fix_master
                    },
                    options: {method: 'GET'},
                    isLoadOnPageInit: false
                }
            },
            queryFormData: {
                groupId: fnr.getQueryString("groupId"),
                code: '',
                name: ''
            },
            isDialog: fnr.getQueryString("componentid__") != undefined,
            componentid__: fnr.getQueryString("componentid__"),
            signType: []
        },
        ready: function () {
            this.loadSignType();
        }
        ,
        methods: {
            queryFormSubmit: function () {
                this.$refs.dtList.query();
            },
            reload: function () {
                this.$refs.dtList.query();
            },
            loadSignType: function () {
                var self = this;
<<<<<<< HEAD
                var defrend = fnr.ajaxJson("../api/supply/getSignType", {}, {method: "GET"});
=======
                var defrend = fnr.ajaxJson("/voucher/api/supply/getSignType", {}, {method: "GET"});
>>>>>>> fix_master
                defrend.then(function (result) {
                    var resp = result.json();
                    if (resp.code == 200) {
                        fnr.each(resp.data, function (k, v) {
                            self.signType[v.value] = v.name;
                        });
                        self.queryFormSubmit();
                    }
                });
            }
        }
    })
    ;
});

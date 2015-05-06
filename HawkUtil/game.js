var globalData = globalData || {},
    WanBaApi = {
        wanbarRecharge: function(c, b, a, d, g, e, h) {
            window.popPayTips({
                is_vip: c,
                openkey: b,
                openid: a,
                appid: d,
                zoneid: g,
                score: e,
                defaultScore: h
            })
        },
        getHttpParams: function(c) {
            c = location.href.match(new RegExp("(\\?|#|&)" + c + "=([^&#]*)(&|#|$)"));
            return decodeURIComponent(c ? c[2] : "")
        },
        shareTo: function() {
            fusion2.dialog.sendStory({
                title: "\u6807\u9898",
                img: "http://app24341.qzoneapp.com/app24341/feed_clown.png",
                summary: "\u7b80\u4ecb",
                msg: "\u70ab\u8000\u7684\u8bdd",
                onShown: function(c) {
                    alert("Shown")
                },
                onClose: function(c) {
                    alert("Closed")
                }
            })
        },
        relogin: function() {
            fusion2.dialog.relogin()
        },
        loginSuccess: function() {
            globalData.platform = WanBaApi.getHttpParams("platform") || 1;
            globalData.pf = WanBaApi.getHttpParams("pf");
            globalData.pfkey = WanBaApi.getHttpParams("pfkey");
            globalData.openid = WanBaApi.getHttpParams("openid");
            globalData.openkey = WanBaApi.getHttpParams("openkey");
            globalData.invkey = WanBaApi.getHttpParams("invkey");
            globalData.iopenid = WanBaApi.getHttpParams("iopenid");
            globalData.itime = WanBaApi.getHttpParams("itime");
            globalData.source = WanBaApi.getHttpParams("source");
            globalData.app_custom = WanBaApi.getHttpParams("app_custom")
        }
    };
WanBaApi.loginSuccess();
var BasePayment = function() {
    function c() {}
    c.prototype.effect = function(b, a, d) {};
    return c
} ();
BasePayment.prototype.__class__ = "BasePayment";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    BaseContainer = function(c) {
        function b() {
            c.call(this);
            this._isDisposed = !1
        }
        __extends(b, c);
        b.prototype._parentChanged = function(a) {
            c.prototype._parentChanged.call(this, a);
            null == a && this.callChildDispose(this)
        };
        b.prototype.callChildDispose = function(a) {
            a instanceof b && a.dispose();
            for (var d = 0; d < a.numChildren; d++) {
                var c = a.getChildAt(d);
                c instanceof egret.DisplayObjectContainer && this.callChildDispose(c)
            }
        };
        b.prototype.dispose = function() {
            this._isDisposed = !0
        };
        return b
    } (egret.DisplayObjectContainer);
BaseContainer.prototype.__class__ = "BaseContainer";
var BasePlatform = function() {
    function c() {}
    c.prototype.init = function() {};
    c.prototype.getPlatformConfig = function() {
        return "payment_wbConfig"
    };
    c.prototype.payment = function(b, a, d) {};
    c.prototype.deserialize = function() {};
    c.prototype.codeAward = function(b) {};
    c.prototype.serialize = function(b) {};
    c.prototype._getPlatformName = function() {
        return "none"
    };
    Object.defineProperty(c.prototype, "name", {
        get: function() {
            return this._getPlatformName()
        },
        enumerable: !0,
        configurable: !0
    });
    c.prototype.getPayments = function() {
        var b = [],
            a = RES.getRes(this.getPlatformConfig()),
            d;
        for (d in a) b.push(new PaymentModel(a[d]));
        return b
    };
    return c
} ();
BasePlatform.prototype.__class__ = "BasePlatform";
var PlatformTypeInfo = function() {
    return function(c, b) {
        this.type = this.types = null;
        this.types = c;
        this.type = b
    }
} ();
PlatformTypeInfo.prototype.__class__ = "PlatformTypeInfo";
var PlatformFactory = function() {
    function c() {
        this._classMapping = [];
        this._curPlatform = null
    }
    c.getInstance = function() {
        null == this._factory && (this._factory = new c);
        return this._factory
    };
    c.prototype.inject = function() {
        for (var b = [], a = 0; a < arguments.length; a++) b[a - 0] = arguments[a];
        this._classMapping.push(new PlatformTypeInfo(b.slice(0, b.length - 1), b[b.length - 1]))
    };
    c.prototype.getType = function(b) {
        for (var a = 0; a < this._classMapping.length; a++) if ( - 1 < this._classMapping[a].types.indexOf(b)) return this._classMapping[a].type;
        return null
    };
    c.prototype.newClass = function(b) {
        return (b = this.getType(b)) ? new b: null
    };
    c.prototype.getPlatform = function() {
        return this._curPlatform
    };
    c.prototype.setPlatform = function(b) {
        this._curPlatform = (b = this.newClass(b)) ? b: new BasePlatform
    };
    c.getPlatform = function() {
        return this.getInstance().getPlatform()
    };
    c.setPlatform = function(b) {
        this.getInstance().setPlatform(b)
    };
    c._factory = null;
    return c
} ();
PlatformFactory.prototype.__class__ = "PlatformFactory";
var globalData = globalData || {},
    platformType_bearjoy = 1,
    platformType_wanba = 2,
    platformType_egret = 3,
    ProxyUtils = function() {
        function c() {}
        c.buyItem = function(b, a, d) {
            function g() {
                var e = JSON.parse(f.data);
                0 == e.s ? a.call(d) : 3 == e.s || 4 == e.s ? c.wanbaRelogin() : c.buyItem(b, a, d);
                f.removeEventListener(egret.Event.COMPLETE, g, this)
            }
            var e = {
                mod: "Store",
                "do": "buy",
                p: {}
            };
            e.p.data = globalData;
            e.p.data.id = b;
            e.p.data.lv = DataCenter.getInstance().totalData.currentLevel;
            var e = "h=" + c.hashKey + "&data=" + JSON.stringify(e),
                h = new egret.URLRequest;
            h.method = egret.URLRequestMethod.GET;
            h.data = new egret.URLVariables(e);
            h.url = c.requestUrl;
            var f = new egret.URLLoader;
            f.addEventListener(egret.Event.COMPLETE, g, this);
            f.load(h)
        };
        c.buyGold = function(b) {
            function a() {
                var d = JSON.parse(e.data);
                0 == d.s ? (c.jifen += d.score, c.isVIP = 0 < d.vip, DataCenter.getInstance().refreshGc(d.gc), doAction(Const.CHANGE_JIFEN)) : 3 == d.s || 4 == d.s ? c.wanbaRelogin() : c.buyGold(b);
                e.removeEventListener(egret.Event.COMPLETE, a, this)
            }
            c.platformInfo == platformType_wanba && "2" == c.platform && (b += 1E3);
            var d = {
                mod: "User",
                "do": "buyZoneItem",
                p: {}
            };
            d.p.data = globalData;
            d.p.data.cId = b;
            var d = "h=" + c.hashKey + "&data=" + JSON.stringify(d),
                g = new egret.URLRequest;
            g.method = egret.URLRequestMethod.POST;
            g.data = new egret.URLVariables(d);
            g.url = c.requestUrl;
            var e = new egret.URLLoader;
            e.addEventListener(egret.Event.COMPLETE, a, this);
            e.load(g)
        };
        c.getTencentCoin = function(b, a) {
            function d() {
                var g = JSON.parse(h.data);
                0 == g.s ? (c.jifen = g.score, c.isVIP = 0 < g.vip, doAction(Const.CHANGE_JIFEN), b && a && a.call(b)) : 3 == g.s || 4 == g.s ? c.wanbaRelogin() : c.getTencentCoin(b, a);
                h.removeEventListener(egret.Event.COMPLETE, d, this)
            }
            void 0 === b && (b = null);
            void 0 === a && (a = null);
            var g = {
                mod: "User",
                "do": "getZoneInfo",
                p: {}
            };
            g.p.data = globalData;
            var g = "h=" + c.hashKey + "&data=" + JSON.stringify(g),
                e = new egret.URLRequest;
            e.method = egret.URLRequestMethod.POST;
            e.data = new egret.URLVariables(g);
            e.url = c.requestUrl;
            var h = new egret.URLLoader;
            h.addEventListener(egret.Event.COMPLETE, d, this);
            h.load(e)
        };
        c.getDiamond = function(b) {
            function a() {
                var b = JSON.parse(g.data);
                0 != b.s && (3 != b.s && 4 != b.s || c.wanbaRelogin());
                g.removeEventListener(egret.Event.COMPLETE, a, this)
            }
            var d = {
                mod: "User",
                "do": "getGc",
                p: {}
            };
            d.p.type = b;
            b = "h=" + c.hashKey + "&data=" + JSON.stringify(d);
            d = new egret.URLRequest;
            d.method = egret.URLRequestMethod.POST;
            d.data = new egret.URLVariables(b);
            d.url = c.requestUrl;
            var g = new egret.URLLoader;
            g.addEventListener(egret.Event.COMPLETE, a, this);
            g.load(d)
        };
        c.reduceDiamond = function(b) {
            function a() {
                var b = JSON.parse(g.data);
                0 != b.s && (3 != b.s && 4 != b.s || c.wanbaRelogin());
                g.removeEventListener(egret.Event.COMPLETE, a, this)
            }
            var d = {
                mod: "User",
                "do": "reduceGc",
                p: {}
            };
            d.p.gc = b;
            b = "h=" + c.hashKey + "&data=" + JSON.stringify(d);
            d = new egret.URLRequest;
            d.method = egret.URLRequestMethod.POST;
            d.data = new egret.URLVariables(b);
            d.url = c.requestUrl;
            var g = new egret.URLLoader;
            g.addEventListener(egret.Event.COMPLETE, a, this);
            g.load(d)
        };
        c.getGold = function(b) {
            function a() {
                var b = JSON.parse(g.data);
                0 != b.s && (3 != b.s && 4 != b.s || c.wanbaRelogin());
                g.removeEventListener(egret.Event.COMPLETE, a, this)
            }
            var d = {
                mod: "Achieve",
                "do": "finish",
                p: {}
            };
            d.p.aId = b;
            b = "h=" + c.hashKey + "&data=" + JSON.stringify(d);
            d = new egret.URLRequest;
            d.method = egret.URLRequestMethod.POST;
            d.data = new egret.URLVariables(b);
            d.url = c.requestUrl;
            var g = new egret.URLLoader;
            g.addEventListener(egret.Event.COMPLETE, a, this);
            g.load(d)
        };
        c.getCodeAward = function(b) {
            function a() {
                var b = JSON.parse(g.data);
                0 == b.s ? (GameMainLayer.instance.hideSetLayer(), GameMainLayer.instance.hideCodeAwardLayer(), doAction(Const.DROP_COIN, {
                    type: 2,
                    num: 4,
                    one: "25"
                })) : 3018 == b.s ? doAction(Const.SHOW_TIPS, "\u60a8\u5df2\u7ecf\u9886\u53d6\u8fc7\u4e86\u5151\u6362\u7801\u5956\u52b1") : 3019 == b.s ? doAction(Const.SHOW_TIPS, "\u8be5\u5151\u6362\u7801\u5df2\u7ecf\u4f7f\u7528\u8fc7\u4e86") : 3043 == b.s && doAction(Const.SHOW_TIPS, "\u60a8\u7684\u5151\u6362\u7801\u683c\u5f0f\u4e0d\u6b63\u786e");
                g.removeEventListener(egret.Event.COMPLETE, a, this)
            }
            var d = {
                mod: "Code",
                "do": "exchange",
                p: {}
            };
            d.p.code = b;
            b = "h=" + c.hashKey + "&data=" + JSON.stringify(d);
            d = new egret.URLRequest;
            d.method = egret.URLRequestMethod.POST;
            d.data = new egret.URLVariables(b);
            d.url = c.requestUrl;
            var g = new egret.URLLoader;
            g.addEventListener(egret.Event.COMPLETE, a, this);
            g.load(d)
        };
        c.saveData = function(b) {
            function a() {
                var b = JSON.parse(g.data);
                0 == b.s ? c.saveTime += 1 : 3 != b.s && 4 != b.s || c.wanbaRelogin();
                g.removeEventListener(egret.Event.COMPLETE, a, this)
            }
            var d = {
                mod: "User",
                "do": "saveInfo",
                p: {}
            };
            d.p.data = globalData;
            d.p.data.saveData = b;
            d.p.data.time = c.saveTime;
            b = "h=" + c.hashKey + "&data=" + JSON.stringify(d);
            d = new egret.URLRequest;
            d.method = egret.URLRequestMethod.POST;
            d.data = new egret.URLVariables(b);
            d.url = c.requestUrl;
            var g = new egret.URLLoader;
            g.addEventListener(egret.Event.COMPLETE, a, this);
            g.addEventListener(egret.IOErrorEvent.IO_ERROR,
                function() {
                    this._errorTime += 1;
                    3 < this._errorTime && GameMainLayer.instance.showMaskLayer()
                },
                this);
            g.load(d)
        };
        c.doReg = function() {
            function b() {
                d.removeEventListener(egret.Event.COMPLETE, b, this);
                var a = JSON.parse(d.data);
                0 == a.s ? (egret.localStorage.setItem("bearjoyTapGameHashKey1", a.h), c.hashKey = a.h, c.getUserGoal()) : c.doReg()
            }
            var a = {
                mod: "User",
                "do": "reg",
                p: {}
            };
            a.p.uName = Math.floor(1E8 * Math.random()) + "@bearjoy.com";
            a.p.uPass = "123456";
            var d = new egret.URLLoader;
            d.addEventListener(egret.Event.COMPLETE, b, this);
            a = "data=" + JSON.stringify(a);
            d.load(new egret.URLRequest(c.requestUrl + "?" + a))
        };
        c.initPlatform = function() {
            globalData.openid && "" != globalData.openid ? globalData.egretPlat ? (c.requestUrl = "http://g1.mix.pregnant.bearjoy.com/api.php", c.platformInfo = platformType_egret) : (c.requestUrl = "http://mezj.gz.1251278653.clb.myqcloud.com/api.php", c.platformInfo = platformType_wanba) : (c.requestUrl = "http://local.bearjoy.tg.bearjoy.com/api.php", c.platformInfo = platformType_bearjoy);
            egret.localStorage.setItem("request_url", c.requestUrl)
        };
        c.login = function(b, a) {
            function d() {
                e.removeEventListener(egret.Event.COMPLETE, d, this);
                var g = JSON.parse(e.data);
                0 == g.s ? (c.hashKey = g.h, egret.localStorage.setItem("hashKey", g.h), b ? b.call(a) : c.getUserGoal()) : 3 == g.s || 4 == g.s ? c.wanbaRelogin() : 104 == g.s ? c.wanbaRelogin() : c.login()
            }
            void 0 === b && (b = null);
            void 0 === a && (a = null);
            this.initPlatform();
            if (c.platformInfo == platformType_bearjoy) this.getUserGoal();
            else {
                var g = {
                    mod: "User",
                    "do": "login",
                    p: {}
                };
                g.p.data = globalData;
                var e = new egret.URLLoader;
                e.addEventListener(egret.Event.COMPLETE, d, this);
                g = "data=" + JSON.stringify(g);
                e.load(new egret.URLRequest(c.requestUrl + "?" + g))
            }
        };
        c.egretLogin = function(b) {
            function a() {
                d.removeEventListener(egret.Event.COMPLETE, a, this);
                var b = JSON.parse(d.data);
                0 == b.s ? (egret.localStorage.setItem("hashKey", b.h), c.hashKey = b.h, c.getUserGoal()) : c.egretLogin()
            }
            void 0 === b && (b = null);
            b && (c.requestUrl = "http://mezj.egret-labs.org/api.php", c.loginUrl = "http://mezj.egret-labs.org/login.php", c.platformInfo = platformType_egret, globalData.token = b, egret.localStorage.setItem("request_url", c.requestUrl));
            var d = new egret.URLLoader;
            d.addEventListener(egret.Event.COMPLETE, a, this);
            d.load(new egret.URLRequest(c.loginUrl + "?" + ("token=" + globalData.token)))
        };
        c.wanbaRelogin = function() {};
        c.getlt = function(b, a, d) {
            function g() {
                var b = JSON.parse(h.data);
                0 == b.s ? a && a.call(d, b.code) : a.call(d, null);
                h.removeEventListener(egret.Event.COMPLETE, g, this)
            }
            void 0 === d && (d = null);
            var e = {
                mod: "Code",
                "do": "getIt",
                p: {}
            };
            e.p.type = b;
            b = "h=" + c.hashKey + "&data=" + JSON.stringify(e);
            e = new egret.URLRequest;
            e.method = egret.URLRequestMethod.POST;
            e.data = new egret.URLVariables(b);
            e.url = c.requestUrl;
            var h = new egret.URLLoader;
            h.addEventListener(egret.Event.COMPLETE, g, this);
            h.load(e)
        };
        c.crossLevel = function(b) {
            function a() {
                0 != JSON.parse(e.data).s && c.crossLevel(b);
                e.removeEventListener(egret.Event.COMPLETE, a, this)
            }
            var d = {
                mod: "Top",
                "do": "updateTime",
                p: {}
            };
            d.p.lv = b;
            var d = "h=" + c.hashKey + "&data=" + JSON.stringify(d),
                g = new egret.URLRequest;
            g.method = egret.URLRequestMethod.POST;
            g.data = new egret.URLVariables(d);
            g.url = c.requestUrl;
            var e = new egret.URLLoader;
            e.addEventListener(egret.Event.COMPLETE, a, this);
            e.load(g)
        };
        c.getUserGoal = function() {
            function b() {
                d.removeEventListener(egret.Event.COMPLETE, b, this);
                var a = JSON.parse(d.data);
                0 == a.s ? (c.platformInfo == platformType_wanba && (c.openid = globalData.openid, c.openkey = globalData.openkey, c.pf = globalData.pf, c.platform = globalData.platform), c.userId = a.i.id, c.awdTo = a.i.awdTo, c.winxinUrl = a.i.wxurl, c.boxUrl = a.i.boxurl, c.serverData = a.i.dt, c.gcNum = a.i.gc, c.nickName = a.i.nNa, c.platUserId = a.i.platUserId ? a.i.platUserId: "", c.gameInited = !0, doAction(Const.INFO_INITED)) : 3 == a.s || 4 == a.s ? c.wanbaRelogin() : c.getUserGoal()
            }
            if (c.platformInfo == platformType_bearjoy) {
                var a = egret.localStorage.getItem("bearjoyTapGameHashKey1");
                if (!a) {
                    c.doReg();
                    return
                }
                egret.localStorage.setItem("hashKey", a);
                c.hashKey = a
            }
            a = {
                mod: "User",
                "do": "getInfo",
                p: {}
            };
            a.p.data = globalData;
            var d = new egret.URLLoader;
            d.addEventListener(egret.Event.COMPLETE, b, this);
            a = "h=" + c.hashKey + "&data=" + JSON.stringify(a);
            d.load(new egret.URLRequest(c.requestUrl + "?" + a))
        };
        c.platformInfo = platformType_bearjoy;
        c.appid = "1104157917";
        c.isVIP = !1;
        c.jifen = -1;
        c.network = null;
        c.hashKey = "";
        c.nickName = "\u5c0f\u6469\u6469";
        c.platUserId = "";
        c.awdTo = {};
        c.winxinUrl = null;
        c.boxUrl = null;
        c.saveTime = 1;
        c.serverData = {};
        c.gcNum = 0;
        c.gameInited = !1;
        c.sfTime = 6;
        c.userId = "";
        c._errorTime = 0;
        return c
    } ();
ProxyUtils.prototype.__class__ = "ProxyUtils";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    SimpleButton = function(c) {
        function b() {
            c.call(this);
            this._currentFrame = 1;
            this._scale = 1.05;
            this._initScaleY = this._initScaleX = 0;
            this._canScale = !1;
            this._frameNumber = 0;
            this._displayName = null;
            this.touchEnabled = this._enabled = !0;
            this._canScale = !1
        }
        __extends(b, c);
        b.prototype.hitTest = function(a, b) {
            return egret.DisplayObject.prototype.hitTest.call(this, a, b)
        };
        b.prototype.changeScale = function(a) {
            this._scale = a
        };
        b.prototype._onAddToStage = function() {
            c.prototype._onAddToStage.call(this);
            this.addListeners();
            this._initScaleX = this.scaleX;
            this._initScaleY = this.scaleY
        };
        Object.defineProperty(b.prototype, "displayName", {
            get: function() {
                return this._displayName
            },
            set: function(a) {
                this._displayName = a
            },
            enumerable: !0,
            configurable: !0
        });
        b.prototype._onRemoveFromStage = function() {
            c.prototype._onRemoveFromStage.call(this);
            this.removeListeners()
        };
        b.prototype.addListeners = function() {
            this.addEventListener(egret.TouchEvent.TOUCH_BEGIN, this.mouseDown, this)
        };
        b.prototype.removeListeners = function() {
            this.removeEventListener(egret.TouchEvent.TOUCH_BEGIN, this.mouseDown, this);
            egret.MainContext.instance.stage.removeEventListener(egret.TouchEvent.TOUCH_END, this.mouseUp, this);
            egret.MainContext.instance.stage.removeEventListener(egret.TouchEvent.TOUCH_MOVE, this.mouseMove, this);
            removeEventsByTarget(this)
        };
        b.prototype.mouseDown = function(a) {
            this._isMoved = !1;
            egret.MainContext.instance.stage.addEventListener(egret.TouchEvent.TOUCH_END, this.mouseUp, this);
            egret.MainContext.instance.stage.addEventListener(egret.TouchEvent.TOUCH_MOVE, this.mouseMove, this);
            this._startX = a.stageX;
            this._startY = a.stageY;
            this.setChoose(!0)
        };
        b.prototype.mouseUp = function() {
            this._isMoved || (this.setChoose(!1), this.onClick());
            egret.MainContext.instance.stage.removeEventListener(egret.TouchEvent.TOUCH_END, this.mouseUp, this);
            egret.MainContext.instance.stage.removeEventListener(egret.TouchEvent.TOUCH_MOVE, this.mouseMove, this)
        };
        b.prototype.mouseMove = function(a) {
            var b = a.stageY;
            10 > Math.abs(a.stageX - this._startX) && 10 > Math.abs(b - this._startY) || (this._isMoved = !0, this.setChoose(!1), egret.MainContext.instance.stage.removeEventListener(egret.TouchEvent.TOUCH_MOVE, this.mouseMove, this))
        };
        b.prototype.addOnClick = function(a, b) {
            this._callBack = a;
            this._target = b
        };
        b.prototype.onClick = function() {
            this._enabled ? this._callBack && this._target && this._callBack.apply(this._target, [this]) : null != this._touchCallback && this._touchCallback.callback();
            doAction(this.name + "Click")
        };
        b.prototype.setEnabled = function(a) {
            this._enabled = a;
            this.touchEnabled = this._touchCallback ? !0 : a;
            this.setEnabledState()
        };
        b.prototype.setEnabledState = function() {
            if (! (1 >= this.numChildren)) {
                var a = this.getChildAt(0),
                    b = this.getChildAt(1);
                this._enabled ? (a.visible = !0, b && (b.visible = !1)) : (a.visible = !1, b && (b.visible = !0))
            }
        };
        b.prototype.setEnableCallback = function(a) {
            this._touchCallback = a;
            this.touchEnabled = null == this._touchCallback ? this._enabled: !0;
            this.setEnabledState()
        };
        b.prototype.useZoomOut = function(a) {
            this._canScale = a
        };
        b.prototype.initFontTextField = function(a) {
            this._textField = a;
            a instanceof egret.TextField && (a.stroke = 2);
            if (a && null != a.parent) {
                if (a.parent == this) return;
                GameUtils.removeFromParent(a)
            }
            this.addChild(a)
        };
        b.prototype.setFontText = function(a, b) {
            void 0 === b && (b = 30);
            if (null == this._textField) {
                this._textField = new egret.TextField;
                this._textField.textColor = 16777215;
                this._textField.textAlign = "center";
                this._textField.fontFamily = "Courier-Bold";
                this._textField.size = b;
                this._textField.stroke = 2;
                this.addChild(this._textField);
                this._textField.anchorX = 0.5;
                this._textField.anchorY = 0.5;
                var c = this.getBounds();
                this._textField.x = c.width / 2;
                this._textField.y = c.height / 2 - 5
            }
            this._textField.text = a
        };
        b.prototype.setFontColor = function(a) {};
        b.prototype.setChoose = function(a, b) {
            void 0 === b && (b = void 0);
            "boolean" == typeof a && (a = a ? 2 : 1);
            "undefined" != typeof b && (this._adjust = !0 === b ? !0 : !1);
            this._adjust && (a = 3);
            this.playZoomOut(a);
            this._enabled && this.setFrameChild(a)
        };
        b.prototype.setFrameChild = function(a) {};
        b.prototype.playZoomOut = function(a) {
            if (this._canScale) {
                var b = 2 != a ? this._initScaleX: this._initScaleX * this._scale;
                a = 2 != a ? this._initScaleY: this._initScaleY * this._scale;
                egret.Tween.get(this).to({
                        scaleX: b,
                        scaleY: a
                    },
                    70)
            }
        };
        return b
    } (egret.DisplayObjectContainer);
SimpleButton.prototype.__class__ = "SimpleButton";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    ProxyErrorCode = function() {
        function c() {}
        c.ERROR_DATA = -1E3;
        c.TIME_OUT = -1001;
        c.ERROR_REQUEST = -1002;
        return c
    } ();
ProxyErrorCode.prototype.__class__ = "ProxyErrorCode";
var Proxy = function(c) {
    function b(a) {
        void 0 === a && (a = {});
        c.call(this);
        this._isTimeout = !1;
        this._timeoutId = null;
        this._customParams = {};
        this._params = this.formatParams(a);
        this._requestUrl = egret.localStorage.getItem("request_url")
    }
    __extends(b, c);
    b.prototype.formatParams = function(a) {
        if (a && !a.hasOwnProperty("do") && a.hasOwnProperty("mod")) {
            var b = a.mod; - 1 < b.indexOf(".") && (b = b.split("."), a.mod = b[0], a["do"] = b[1])
        }
        return a
    };
    Object.defineProperty(b.prototype, "requestUrl", {
        get: function() {
            return this._requestUrl
        },
        set: function(a) {
            this._requestUrl = a
        },
        enumerable: !0,
        configurable: !0
    });
    b.prototype.paramsToQueryString = function() {
        for (var a = [], b = 0; b < arguments.length; b++) a[b - 0] = arguments[b];
        for (var b = [], c = 0; c < a.length; c++) {
            var e = a[c],
                h;
            for (h in e) b.push(h + "=" + e[h])
        }
        return b.join("&")
    };
    b.prototype.load = function() {
        var a = this,
            d = -1 == this._requestUrl.indexOf("?") ? this._requestUrl + "?": this._requestUrl;
        "?" != d[d.length - 1] && "&" != d[d.length - 1] && (d += "&");
        var c = applyFilters("load_proxy", this);
        this._status = ProxyStatus.REQUEST;
        doAction("proxy_status_reqeust", this);
        if (c === this || !0 === c) {
            this._status = ProxyStatus.WAIT;
            doAction("proxy_status_wait", this);
            var c = egret.localStorage.getItem("hashKey"),
                e = this.getParamString(),
                h = e + "xsanguox888~1f2a3",
                f = new md5;
            this.addParam("s", f.hex_md5(h));
            c = this.paramsToQueryString({
                    h: c,
                    data: e
                },
                this._customParams, b._globalParams);
            d += c;
            c = new egret.URLRequest;
            c.method = egret.URLRequestMethod.POST;
            c.data = new egret.URLVariables(d.substring(d.lastIndexOf("?") + 1, d.length));
            c.url = d.substring(0, d.lastIndexOf("?"));
            var k = new egret.URLLoader;
            k.addEventListener(egret.IOErrorEvent.IO_ERROR, this.onError, this);
            k.addEventListener(egret.Event.COMPLETE, this.onComplete, this);
            this._timeoutId = Time.setTimeout(function() {
                    k.removeEventListener(egret.IOErrorEvent.IO_ERROR, a.onError, a);
                    k.removeEventListener(egret.Event.COMPLETE, a.onComplete, a);
                    a._errorCode = ProxyErrorCode.TIME_OUT;
                    a._errorMessage = "\u8bf7\u6c42\u8d85\u65f6";
                    a._isTimeout = !0;
                    doAction("proxy_timeout", a);
                    a.dispatchEvent(new ProxyEvent(ProxyEvent.TIME_OUT, a));
                    a.dispatchEvent(new ProxyEvent(ProxyEvent.ERROR, a))
                },
                this, b._timeout);
            k.load(c)
        }
    };
    Object.defineProperty(b.prototype, "isTimeout", {
        get: function() {
            return this._isTimeout
        },
        enumerable: !0,
        configurable: !0
    });
    b.prototype.onError = function(a) {
        this._isRequestSucceed = !1;
        this._errorMessage = "\u8bf7\u6c42\u5931\u8d25";
        this._errorCode = ProxyErrorCode.ERROR_REQUEST;
        Time.clearTimeout(this._timeoutId);
        this.dispatchEvent(new ProxyEvent(ProxyEvent.REQUEST_FAIL, this));
        this.dispatchEvent(new ProxyEvent(ProxyEvent.ERROR, this));
        doAction("proxy_request_error", this);
        doAction("proxy_status_response", this)
    };
    b.prototype.onResponse = function(a) {
        this._responseData = a;
        this._isResponseSucceed = !1;
        this._status = ProxyStatus.RESPONSE;
        this._responseData && this._responseData.hasOwnProperty("s") && (this._isResponseSucceed = 0 == this._responseData.s);
        this.dispatchEvent(new ProxyEvent(ProxyEvent.REQUEST_SUCCEED, this));
        this._errorCode = this._responseData ? this._responseData.s: ProxyErrorCode.ERROR_DATA;
        this._isRequestSucceed = !0;
        this._isResponseSucceed ? (doAction("proxy_response_succeed", this), this.dispatchEvent(new ProxyEvent(ProxyEvent.RESPONSE_SUCCEED, this))) : (doAction("proxy_response_error", this), this.dispatchEvent(new ProxyEvent(ProxyEvent.RESPONSE_ERROR, this)), this.dispatchEvent(new ProxyEvent(ProxyEvent.ERROR, this)));
        doAction("proxy_request_succeed", this);
        doAction("proxy_status_response", this)
    };
    b.prototype.onComplete = function(a) {
        Time.clearTimeout(this._timeoutId);
        a = a.target;
        var b = null;
        try {
            b = JSON.parse(a.data)
        } catch(c) {}
        this.onResponse(b)
    };
    b.prototype.getURLVariables = function(a) {
        var b = [],
            c;
        for (c in a) b.push(c + "=" + a[c]);
        a = b.join("&");
        return new egret.URLVariables(a)
    };
    b.prototype.getParamByName = function(a) {
        return this._params ? this._params[a] : null
    };
    b.prototype.hasParamByName = function(a) {
        return this._params ? this._params.hasOwnProperty(a) : !1
    };
    Object.defineProperty(b.prototype, "params", {
        get: function() {
            return this._params
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(b.prototype, "responseData", {
        get: function() {
            return this._responseData
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(b.prototype, "isResponseSucceed", {
        get: function() {
            return this._isResponseSucceed
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(b.prototype, "isRequestSucceed", {
        get: function() {
            return this._isRequestSucceed
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(b.prototype, "errorMessage", {
        get: function() {
            return this._errorMessage
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(b.prototype, "errorCode", {
        get: function() {
            return this._errorCode
        },
        enumerable: !0,
        configurable: !0
    });
    b.prototype.addParam = function(a, b) {
        this._customParams[a] = b
    };
    b.prototype.getParamString = function() {
        return null
    };
    b.addGlobalParams = function(a, b) {
        this._globalParams[a] = b
    };
    b.removeGlobalParams = function(a) {
        delete this._globalParams[a]
    };
    b._timeout = 6E5;
    b._globalParams = {};
    return b
} (egret.EventDispatcher);
Proxy.prototype.__class__ = "Proxy";
var PaymentFactory = function() {
    function c() {
        this._classMapping = {}
    }
    c.prototype.inject = function(b, a) {
        this._classMapping[b] = a
    };
    c.prototype.newClass = function(b) {
        return (b = this._classMapping[b]) ? new b: null
    };
    c.prototype.effect = function(b, a, d) {
        void 0 === a && (a = null);
        void 0 === d && (d = null);
        var c = this.newClass(b.type);
        null != c && c.effect(b, a, d)
    };
    c.getInstance = function() {
        null == this._inst && (this._inst = new c);
        return this._inst
    };
    c._inst = null;
    return c
} ();
PaymentFactory.prototype.__class__ = "PaymentFactory";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    SkillPayment = function(c) {
        function b() {
            c.apply(this, arguments);
            this._buying = !1
        }
        __extends(b, c);
        b.prototype.effect = function(a, b, c) {
            var e = this;
            void 0 === c && (c = null);
            0 == DataCenter.getInstance().paymentCountdown.getTime("payment_" + a.id) && (DataCenter.getInstance().paymentCountdown.setTime("payment_" + a.id, 5), this._model = a, DataCenter.getInstance().hasEnoughGc(a.need) ? ProxyUtils.buyItem(a.id,
                function() {
                    DataCenter.getInstance().reduceGc(a.need, !0);
                    e.releaseSkill();
                    b && b.call(c)
                },
                this) : GameMainLayer.instance.showPaymentRecommendLayer())
        };
        b.prototype.releaseSkill = function() {};
        return b
    } (BasePayment);
SkillPayment.prototype.__class__ = "SkillPayment";
var WXShareBase = function() {
    function c() {}
    c.prototype.getShare = function() {
        return PlatformFactory.getPlatform().getShare()
    };
    c.prototype.loadShareInfo = function(b, a, d, c) {
        void 0 === a && (a = null);
        void 0 === d && (d = null);
        void 0 === c && (c = !1);
        var e = null,
            h = !0;
        ProxyCache.isCache("Protect.getJsapiInfo") ? e = new SingleProxy({
            mod: "User",
            "do": "getShareInfo",
            cache: c,
            p: {
                data: b
            }
        }) : (h = !1, e = new MultiProxy({
                mod: "Protect",
                "do": "getJsapiInfo",
                p: {
                    data: {
                        url: encodeURIComponent(location.href)
                    }
                }
            },
            {
                mod: "User",
                "do": "getShareInfo",
                cache: c,
                p: {
                    data: b
                }
            }));
        e.addEventListener(ProxyEvent.RESPONSE_SUCCEED,
            function(b) {
                b = b.responseData;
                if (!h) {
                    var c = b.Protect.getJsapiInfo,
                        g = new BodyConfig;
                    g.appId = "wxa725d9c20ec312ae";
                    g.nonceStr = c.nonceStr;
                    g.signature = c.signature;
                    g.timestamp = c.timestamp;
                    g.jsApiList = "onMenuShareTimeline onMenuShareAppMessage onMenuShareQQ onMenuShareWeibo showMenuItems chooseWXPay".split(" ");
                    wx.config(g);
                    b = b.User.getShareInfo
                }
                a && a.call(d, b)
            },
            this);
        e.load()
    };
    c.prototype.shareFriend = function(b, a, d) {
        var c = this;
        this.loadShareInfo(b,
            function(b) {
                var h = new BodyMenuShareAppMessage;
                h.title = b.title;
                h.desc = b.desc;
                h.link = b.link;
                h.imgUrl = b.imgUrl;
                h.complete = d.bind(c);
                h.success = a.bind(c);
                wx.onMenuShareAppMessage(h)
            })
    };
    c.prototype.onMenuShareTimeline = function(b) {
        var a = new BodyMenuShareTimeline;
        a.title = b.title;
        a.imgUrl = b.imgUrl;
        a.link = b.link;
        wx.onMenuShareTimeline(a)
    };
    c.prototype.onMenuShareAppMessage = function(b) {
        var a = new BodyMenuShareAppMessage;
        a.title = b.title;
        a.desc = b.desc;
        a.link = b.link;
        a.imgUrl = b.imgUrl;
        wx.onMenuShareAppMessage(a)
    };
    c.prototype.onMenuShareQQ = function(b) {
        var a = new BodyMenuShareQQ;
        a.title = b.title;
        a.desc = b.desc;
        a.link = b.link;
        a.imgUrl = b.imgUrl;
        wx.onMenuShareQQ(a)
    };
    c.prototype.onMenuShareWeibo = function(b) {
        var a = new BodyMenuShareWeibo;
        a.title = b.title;
        a.desc = b.desc;
        a.link = b.link;
        a.imgUrl = b.imgUrl;
        wx.onMenuShareWeibo(a)
    };
    c.prototype.removeMask = function() {
        GameMainLayer.instance.hideShareTipLayer()
    };
    return c
} ();
WXShareBase.prototype.__class__ = "WXShareBase";
var ItemsTouchType; (function(c) {
    c[c.BEGAN = 0] = "BEGAN";
    c[c.END = 1] = "END";
    c[c.MOVED = 2] = "MOVED";
    c[c.TOUCH = 3] = "TOUCH"
})(ItemsTouchType || (ItemsTouchType = {}));
var ItemsTouchManager = function() {
    function c() {
        this._items = [];
        this._regions = [];
        egret.MainContext.instance.stage.addEventListener(egret.TouchEvent.TOUCH_MOVE, this.onTouchMove, this);
        egret.MainContext.instance.stage.addEventListener(egret.TouchEvent.TOUCH_END, this.onTouchEnd, this)
    }
    c.prototype.clearRegions = function() {
        this._regions = []
    };
    c.prototype.onTouchBegin = function(b) {
        this._touchPoint = new egret.Point(b.stageX, b.stageY);
        this._touchDisplayObject = b.currentTarget;
        this.message(0, this._touchDisplayObject, this._touchPoint, null)
    };
    c.prototype.onTouchMove = function(b) {
        null != this._touchDisplayObject && (b = new egret.Point(b.stageX, b.stageY), this.message(2, this._touchDisplayObject, b, null))
    };
    c.prototype.addItems = function() {
        for (var b = [], a = 0; a < arguments.length; a++) b[a - 0] = arguments[a];
        for (a = 0; a < b.length; a++) {
            var d = b[a];
            d.touchEnabled = !0;
            this.addDisplayListener(d);
            this._items.push(d)
        }
    };
    c.prototype.removeItems = function() {
        for (var b = [], a = 0; a < arguments.length; a++) b[a - 0] = arguments[a];
        for (a = 0; a < b.length; a++) {
            var d = b[a];
            d.touchEnabled = !1;
            this.removeDisplayListener(d)
        }
    };
    c.prototype.addDisplayListener = function(b) {
        b.addEventListener(egret.TouchEvent.TOUCH_BEGIN, this.onTouchBegin, this)
    };
    c.prototype.removeDisplayListener = function(b) {
        b.removeEventListener(egret.TouchEvent.TOUCH_BEGIN, this.onTouchBegin, this)
    };
    c.prototype.addRegions = function() {
        for (var b = [], a = 0; a < arguments.length; a++) b[a - 0] = arguments[a];
        for (a = 0; a < b.length; a++) this._regions.push(b[a])
    };
    c.prototype.removeRegions = function() {
        for (var b = 0; b < arguments.length; b++);
    };
    c.prototype.onTouchEnd = function(b) {
        if (null != this._touchDisplayObject) {
            b = new egret.Point(b.stageX, b.stageY);
            for (var a = null,
                     d = 0; d < this._regions.length; d++) {
                var c = this._regions[d];
                if (this.isInside(b, c)) {
                    a = c;
                    break
                }
            }
            this.message(1, this._touchDisplayObject, b, a);
            this.checkTouch(b);
            this._touchDisplayObject = null
        }
    };
    c.prototype.checkTouch = function(b) {
        14 > egret.Point.distance(this._touchPoint, b) && this.message(3, this._touchDisplayObject, b, null)
    };
    c.prototype.message = function(b, a, d, c) {
        this._touchController && this._touchController.message(b, a, d, c);
        this._selector && this._context && this._selector.call(this._context, b, a, d, c)
    };
    c.prototype.isInside = function(b, a) {
        var d = a.localToGlobal(0, 0);
        return (new egret.Rectangle(d.x, d.y, a.width, a.height)).containsPoint(b)
    };
    c.prototype.setCallback = function(b, a) {
        this._selector = b;
        this._context = a
    };
    c.prototype.setController = function(b) {
        this._touchController = b
    };
    c.prototype.clearItems = function() {
        for (; 0 < this._items.length;) {
            var b = this._items.shift();
            this.removeDisplayListener(b)
        }
    };
    c.prototype.dispose = function() {
        this.clearItems();
        egret.MainContext.instance.stage.removeEventListener(egret.TouchEvent.TOUCH_MOVE, this.onTouchMove, this);
        egret.MainContext.instance.stage.removeEventListener(egret.TouchEvent.TOUCH_END, this.onTouchEnd, this);
        removeEventsByTarget(this);
        this._context = this._selector = null;
        this._touchController && (this._touchController.dispose(), this._touchController = null)
    };
    return c
} ();
ItemsTouchManager.prototype.__class__ = "ItemsTouchManager";
var GameAnimation = function() {
    function c(b, a) {
        void 0 === a && (a = null);
        this._overTime = 0;
        this._duration = b;
        this._ease = a
    }
    c.prototype._tick = function(b) {
        this._overTime += b;
        b = this._overTime / this._duration;
        var a = 1 > b ? b: 1,
            a = 0 < a ? a: 0;
        this._ease && (b = this._ease(a));
        this.update(b);
        1 <= a && (this._selector && this._selector.apply(this._context, this._args), this.onComplete())
    };
    c.prototype.onComplete = function() {
        AnimationClear.removeAnimation(this)
    };
    c.prototype.getTarget = function() {
        return this._target
    };
    c.prototype.dispose = function() {
        egret.Ticker.getInstance().unregister(this._tick, this);
        this._context = this._selector = null
    };
    c.prototype.setComplete = function(b, a) {
        void 0 === a && (a = null);
        for (var d = [], c = 2; c < arguments.length; c++) d[c - 2] = arguments[c];
        this._selector = b;
        this._context = a;
        this._args = d;
        return this
    };
    c.prototype.update = function(b) {};
    c.prototype._run = function(b, a) {
        void 0 === a && (a = !0);
        this._target = b;
        a && egret.Ticker.getInstance().register(this._tick, this);
        AnimationClear.addAnimation(this)
    };
    return c
} ();
GameAnimation.prototype.__class__ = "GameAnimation";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    Direction = function() {
        function c() {}
        c.CENTER = "center";
        c.LEFT = "left";
        c.RIGHT = "right";
        c.TOP = "top";
        c.BOTTOM = "bottom";
        c.BOTH = "both";
        c.HORIZONTAL = "horizontal";
        c.VERTICAL = "vertical";
        return c
    } ();
Direction.prototype.__class__ = "Direction";
var ScrollView = function(c) {
    function b() {
        c.call(this);
        this._direction = Direction.VERTICAL;
        this._initY = this._initX = 0;
        this.vScroll = this.hScroll = !0;
        this.animating = this.moved = !1;
        this.startTime = this.pointY = this.pointX = this.startY = this.startX = this.dirY = this.dirX = this.absDistY = this.absDistX = this.distY = this.distX = 0;
        this.steps = [];
        this.lockMove = !1;
        this._minY = this._minX = this._moveCount = 0;
        this.touchEnabled = !0;
        this.options = {
            x: 0,
            y: 0,
            bounce: !0,
            bounceLock: !1,
            momentum: !0,
            useTransform: !0
        };
        this.scrollRect = new egret.Rectangle(0, 0, 100, 100)
    }
    __extends(b, c);
    Object.defineProperty(b.prototype, "direction", {
        get: function() {
            return this._direction
        },
        set: function(a) {
            this._direction = a;
            switch (this._direction) {
                case Direction.BOTH:
                    this.vScroll = this.hScroll = !0;
                    break;
                case Direction.HORIZONTAL:
                    this.hScroll = !0;
                    this.vScroll = !1;
                    break;
                case Direction.VERTICAL:
                    this.hScroll = !1,
                        this.vScroll = !0
            }
        },
        enumerable: !0,
        configurable: !0
    });
    b.prototype._onAddToStage = function() {
        c.prototype._onAddToStage.call(this);
        this.scrollStart()
    };
    b.prototype._onRemoveFromStage = function() {
        c.prototype._onRemoveFromStage.call(this);
        this.removeListeners()
    };
    b.prototype._setWidth = function(a) {
        c.prototype._setWidth.call(this, a);
        this._viewWidth = a;
        this.scrollRect.width = a;
        this.resetGraphics()
    };
    b.prototype._setHeight = function(a) {
        c.prototype._setHeight.call(this, a);
        this._viewHeight = a;
        this.scrollRect.height = a;
        this.resetGraphics()
    };
    b.prototype.resetGraphics = function() {
        this.graphics.clear();
        this.graphics.beginFill(16777215, 0);
        this.graphics.drawRect(0, 0, this._viewWidth, this._viewHeight);
        this.graphics.endFill()
    };
    b.prototype.setContainer = function(a, b, c) {
        this._container && this._container.parent && this._container.parent.removeChild(this._container);
        this._container = a;
        this._initWidth = b;
        this._initHeight = c;
        this._container.touchEnabled = !0;
        if (null != this._container.parent) if (this._container.parent != this) this._container.parent.removeChild(this._container);
        else return;
        this.addChildAt(this._container, 0)
    };
    b.prototype.getInitWidth = function() {
        return this._initWidth || this._container.width
    };
    b.prototype.getInitHeight = function() {
        return this._initHeight || this._container.height
    };
    b.prototype.maxScrollX = function() {
        return Math.min( - this.getInitWidth() + this.width, 0)
    };
    b.prototype.maxScrollY = function() {
        return Math.min( - this.getInitHeight() + this.height, 0)
    };
    b.prototype._start = function(a) {
        if (!this.lockMove) {
            this.animating = this.moved = !1;
            this.dirY = this.dirX = this.absDistY = this.absDistX = this.distY = this.distX = 0;
            var b = this._getPos();
            if (b.x != this._initX || b.y != this._initY) this.steps = [],
                this._setPos(b.x, b.y);
            this.startX = this._initX;
            this.startY = this._initY;
            this.pointX = a.stageX;
            this.pointY = a.stageY;
            this.startTime = egret.getTimer();
            egret.MainContext.instance.stage.addEventListener(egret.TouchEvent.TOUCH_END, this._end, this);
            egret.MainContext.instance.stage.addEventListener(egret.TouchEvent.TOUCH_MOVE, this._move, this)
        }
    };
    b.prototype._move = function(a) {
        egret.MainContext.instance.stage.touchChildren = !1;
        var b = a.stageX - this.pointX,
            c = a.stageY - this.pointY,
            e = this._initX + b,
            h = this._initY + c,
            f = egret.getTimer();
        this.pointX = a.stageX;
        this.pointY = a.stageY;
        if (0 < e || e < this.maxScrollX()) e = this.options.bounce ? this._initX + b / 2 : 0 <= e || 0 <= this.maxScrollX() ? 0 : this.maxScrollX();
        if (0 < h || h < this.maxScrollY()) h = this.options.bounce ? this._initY + c / 2 : 0 <= h || 0 <= this.maxScrollY() ? 0 : this.maxScrollY();
        this.distX += b;
        this.distY += c;
        this.absDistX = Math.abs(this.distX);
        this.absDistY = Math.abs(this.distY);
        6 > this.absDistX && 6 > this.absDistY || (this.moved = !0, this._setPos(e, h), this.dirX = 0 < b ? -1 : 0 > b ? 1 : 0, this.dirY = 0 < c ? -1 : 0 > c ? 1 : 0, 300 < f - this.startTime && (this.startTime = f, this.startX = this._initX, this.startY = this._initY), this._moveCount++, 0 == this._moveCount % 3 && (this._moveCount = 0))
    };
    b.prototype._end = function(a) {
        egret.MainContext.instance.stage.touchChildren = !0;
        egret.MainContext.instance.stage.removeEventListener(egret.TouchEvent.TOUCH_END, this._end, this);
        egret.MainContext.instance.stage.removeEventListener(egret.TouchEvent.TOUCH_MOVE, this._move, this);
        var b = {
                dist: 0,
                time: 0
            },
            c = {
                dist: 0,
                time: 0
            },
            e = egret.getTimer() - this.startTime;
        a = this._initX;
        var h = this._initY;
        if (this.moved) {
            if (300 > e && this.options.momentum) {
                b = a ? this._momentum(a - this.startX, e, -this._initX, this.getInitWidth() - this.width + this._initX, this.options.bounce ? this.width: 0) : b;
                c = h ? this._momentum(h - this.startY, e, -this._initY, 0 > this.maxScrollY() ? this.getInitHeight() - this.height + this._initY: 0, this.options.bounce ? this.height: 0) : c;
                a = this._initX + b.dist;
                h = this._initY + c.dist;
                if (0 < this._initX && 0 < a || this._initX < this.maxScrollX() && a < this.maxScrollX()) b = {
                    dist: 0,
                    time: 0
                };
                if (0 < this._initY && 0 < h || this._initY < this.maxScrollY() && h < this.maxScrollY()) c = {
                    dist: 0,
                    time: 0
                }
            }
            b.dist || c.dist ? (b = Math.max(Math.max(b.time, c.time), 10), this.scrollTo(this.mround(a), this.mround(h), b)) : this._fixPos(200)
        } else this._fixPos(200)
    };
    b.prototype.moveList = function() {
        this._container.youhua && this._container.youhua()
    };
    b.prototype.setScrollRect = function(a, b, c) {
        void 0 === c && (c = 0);
        0 == c ? this._setPos( - (a - this._minX), -(b - this._minY)) : this.scrollTo( - a, -b, c)
    };
    b.prototype.setMinPosition = function(a, b) {
        this._minX = Math.max(a, 0);
        this._minY = Math.max(b, 0)
    };
    b.prototype._setPos = function(a, b) {
        this.hScroll ? (this._container.x = a - this._minX, this._initX = a) : 0;
        this.vScroll ? (this._container.y = b - this._minY, this._initY = b) : 0;
        this.moveList()
    };
    b.prototype._getPos = function() {
        return {
            x: 1 * (this._container.x + this._minX),
            y: 1 * (this._container.y + this._minY)
        }
    };
    b.prototype.mround = function(a) {
        return a >> 0
    };
    b.prototype._fixPos = function(a) {
        var b = this.getFixPosY(),
            c = this.getFixPosX();
        c == this._initX && b == this._initY ? this.moved && (this.moved = !1) : this.scrollTo(c, b, a || 0)
    };
    b.prototype.getFixPosY = function() {
        return 0 <= this._initY || 0 < this.maxScrollY() ? 0 : this._initY < this.maxScrollY() ? this.maxScrollY() : this._initY
    };
    b.prototype.getFixPosX = function() {
        return 0 <= this._initX ? 0 : this._initX < this.maxScrollX() ? this.maxScrollX() : this._initX
    };
    b.prototype.scrollTo = function(a, b, c) {
        this._stopAni();
        this.steps.push({
            x: a,
            y: b,
            time: c || 0
        });
        this._startAni()
    };
    b.prototype._startAni = function() {
        var a = this,
            b = this._initX,
            c = this._initY,
            e = Date.now(),
            h,
            f,
            k;
        this.animating || (this.steps.length ? (h = this.steps.shift(), h.x == b && h.y == c && (h.time = 0), this.moved = this.animating = !0, k = function() {
            var l = Date.now(),
                m;
            l >= e + h.time ? (a._setPos(h.x, h.y), a.animating = !1, a._startAni()) : (l = (l - e) / h.time - 1, f = Math.sqrt(1 - l * l), l = (h.x - b) * f + b, m = (h.y - c) * f + c, a._setPos(l, m), a.animating && egret.callLater(k, this))
        },
            k()) : this._fixPos(400))
    };
    b.prototype._stopAni = function() {
        this.steps = [];
        this.animating = this.moved = !1
    };
    b.prototype._momentum = function(a, b, c, e, h) {
        b = Math.abs(a) / b;
        var f = b * b / 0.0012;
        0 < a && f > c ? (c += h / (6 / (f / b * 6E-4)), b = b * c / f, f = c) : 0 > a && f > e && (e += h / (6 / (f / b * 6E-4)), b = b * e / f, f = e);
        return {
            dist: f * (0 > a ? -1 : 1),
            time: this.mround(b / 6E-4)
        }
    };
    b.prototype.scrollStart = function() {
        this.addEventListener(egret.TouchEvent.TOUCH_BEGIN, this._start, this)
    };
    b.prototype.removeListeners = function() {
        this.removeEventListener(egret.TouchEvent.TOUCH_BEGIN, this._start, this);
        egret.MainContext.instance.stage.removeEventListener(egret.TouchEvent.TOUCH_END, this._end, this);
        egret.MainContext.instance.stage.removeEventListener(egret.TouchEvent.TOUCH_MOVE, this._move, this)
    };
    Object.defineProperty(b.prototype, "useOverflow", {
        set: function(a) {
            this.options.bounce = a
        },
        enumerable: !0,
        configurable: !0
    });
    return b
} (egret.Sprite);
ScrollView.prototype.__class__ = "ScrollView";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    TabView = function(c) {
        function b() {
            c.call(this);
            this._callArr = [];
            this._btnArray = [];
            this._tabHashtable = {};
            this._selectedTab = null;
            this._disableTab = {}
        }
        __extends(b, c);
        b.prototype.init = function() {
            this._callArr = [];
            this._currentTag = 0;
            for (var a = 1,
                     b = 0; b < this.numChildren; b++) {
                var c = this.getChildAt(b);
                if (c instanceof SimpleButton) {
                    var e = this,
                        h = function() {
                            var a = e._callArr.indexOf(this);
                            e.chooseIdx(a + 1)
                        },
                        f = this._formatKey(c.name);
                    this[f] = c;
                    this._tabHashtable[a] = f;
                    this._callArr.push(h);
                    this._btnArray.push(c);
                    c.addOnClick(h, h);
                    c.useZoomOut(!1);
                    a++
                }
            }
        };
        b.prototype._formatKey = function(a) {
            return a.split("Button")[0].toLowerCase()
        };
        b.prototype.setTabItemDisable = function(a, b) {
            this._disableTab[a] = b
        };
        b.prototype.unSelectAll = function() {
            for (var a = 0; a < this._btnArray.length; a++) this._btnArray[a].setChoose(1, !1);
            this._currentTag = 0
        };
        b.prototype.choseIdxState = function(a) {
            this._currentTag = a + 1;
            for (var b = 0; b < this._btnArray.length; b++) {
                var c = this._btnArray[b];
                b + 1 == a ? c.setChoose(3, !0) : c.setChoose(1, !1)
            }
        };
        b.prototype.chooseIdx = function(a) {
            var b = this._tabHashtable[a];
            if (this._disableTab[a]) console.log("\u672a\u5f00\u542f");
            else if (this._currentTag != a + 1 && (!this._selector || this._selector.call(this._context, b))) {
                this._oldType = this._selectedTab ? this._tabHashtable[this._currentTag - 1] : b;
                this._currentTag = a + 1;
                for (var c = 0; c < this._btnArray.length; c++) {
                    var e = this._btnArray[c];
                    c + 1 == a ? (e.setChoose(3, !0), this._selectedTab = e) : e.setChoose(1, !1)
                }
                this.onSelectedTab(b, this[b], this[this._oldType])
            }
        };
        b.prototype.setDefaultTag = function(a) {
            this.chooseIdx(a - 1)
        };
        b.prototype.addOnClick = function(a, b) {
            this._callBack = a;
            this._target = b
        };
        Object.defineProperty(b.prototype, "tabHashtable", {
            get: function() {
                return this._tabHashtable
            },
            enumerable: !0,
            configurable: !0
        });
        b.prototype.useZoomOut = function(a) {
            for (var b = 0; b < this._btnArray.length; b++) this._btnArray[b].useZoomOut(a)
        };
        b.prototype.getCurrentTag = function() {
            var a = Math.max(this._currentTag, 1);
            return a = Math.min(a, this.numChildren)
        };
        b.prototype.setFilterCallback = function(a, b) {
            void 0 === b && (b = null);
            this._selector = a;
            this._context = b
        };
        b.prototype.onSelectedTab = function(a, b, c) {
            this._callBack && this._target && this._callBack.call(this._target, a, b, c)
        };
        b.prototype.selectTabWithType = function(a) {
            for (var b = 1; b <= this._btnArray.length; b++) if (this._tabHashtable[b] == a) {
                this.chooseIdx(b);
                break
            }
        };
        return b
    } (egret.DisplayObjectContainer);
TabView.prototype.__class__ = "TabView";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    Hero = function(c) {
        function b(a) {
            c.call(this);
            this._attackIdx = this._speed = 0;
            this.init(a)
        }
        __extends(b, c);
        b.prototype.init = function(a) {
            var b = this;
            this._hero = a;
            this._action = null;
            1013 > this._hero.configId ? GameUtils.preloadHeroMovieResource(this._hero.config.animationID,
                function() {
                    b._action = new Movie("hero_" + b._hero.config.animationID + "_json", "hero_" + b._hero.config.animationID);
                    b._action.play("stand");
                    b._action.autoRemove = !1;
                    b._action.setCallback(b.onMoveCallback, b);
                    b.addChild(b._action)
                }) : (this._heroIcon = ResourceUtils.createBitmapFromSheet("hero_pic_" + this._hero.configId, "iconRes"), this.addChild(this._heroIcon), this._heroIcon.anchorY = 1, this._heroIcon.anchorX = 0.5);
            this._speed = a.speed;
            this._hero.isMainHero() || (this.attack(), this._timeId = Time.setInterval(this.attack, this, this._speed));
            addAction(Const.CHANGE_SPEED, this.changeSpeed, this);
            addAction(Const.HERO_DEAD, this.heroDead, this)
        };
        b.prototype.heroDead = function(a) {
            a.configId == this._hero.configId && this.parent.removeChild(this)
        };
        b.prototype.onMoveCallback = function() {
            for (var a = [], b = 0; b < arguments.length; b++) a[b - 0] = arguments[b];
            a[0] == Movie.PLAY_COMPLETE && this._action.play("stand")
        };
        b.prototype.changeSpeed = function(a) {
            this._hero.isMainHero() || (Time.clearInterval(this._timeId), this._timeId = Time.setInterval(this.attack, this, this._hero.speed / a))
        };
        b.prototype.attack = function() {
            this._hero.isMainHero() ? (this._attackIdx += 1, 4 < this._attackIdx && (this._attackIdx = 1), this._action && this._action.play("attack" + this._attackIdx)) : this._action && this._action.play("attack")
        };
        b.prototype.dispose = function() {
            c.prototype.dispose.call(this);
            this._timeId && Time.clearInterval(this._timeId);
            removeEventsByTarget(this);
            this._action && (this._action.dispose(), this._action = null)
        };
        return b
    } (BaseContainer);
Hero.prototype.__class__ = "Hero";
var __moo_funciton_context = this,
    moo; (function(c) {
    c.isArray = function(b) {
        return Array.isArray ? Array.isArray(b) : "[object Array]" == b.toString()
    };
    c.isString = function(b) {
        return "string" == typeof b
    };
    c.fileSizeFormat = function(b) {
        var a = [1073741824, 1048576, 1024, 1],
            d = ["GB", "MB", "KB", "B"],
            c,
            e,
            h;
        if (0 === b) return "0 " + d[d.length - 1];
        e = b;
        h = d.length >= a.length ? d[a.length - 1] : "";
        for (c = 0; c < a.length; c++) if (b >= a[c]) {
            e = (b / a[c]).toFixed(2);
            h = d.length >= c ? " " + d[c] : "";
            break
        }
        return e + h
    };
    c.isEmpty = function(b) {
        if (null == b || void 0 === b) return ! 0;
        if (c.isArray(b) || c.isString(b)) return 0 === b.length;
        var a = !1,
            d;
        for (d in b) {
            a = !0;
            break
        }
        return a
    };
    c.isEmptyTemp = function(b) {
        if (null == b || void 0 === b) return ! 0;
        if (c.isArray(b) || c.isString(b)) return 0 === b.length;
        for (var a in b) if (b.hasOwnProperty(a)) return ! 1;
        return ! 0
    };
    c.isUndefined = function(b) {
        return void 0 === b
    };
    c.isArguments = function(b) {
        return ! (!b || !b.hasOwnProperty("callee"))
    };
    c.isFunction = function(b) {
        return "function" === typeof b
    };
    c.isObject = function(b) {
        return b === Object(b)
    };
    c.identity = function(b) {
        return b
    };
    c.getWinWidth = function() {
        return egret.MainContext.instance.stage.stageWidth
    };
    c.getWinHeight = function() {
        return egret.MainContext.instance.stage.stageHeight
    }
})(moo || (moo = {}));
function formatFunctionParam(c, b) {
    return moo.isUndefined(c) ? b: c
}
function _t(c) {
    for (var b = 1; b < arguments.length; b++);
    return null == c ? "": c
}
function _r(c) {
    return c
}
function callUserFunction(c, b, a) {
    a = formatFunctionParam(a, __moo_funciton_context);
    return moo.isFunction(c) ? c.apply(a, b) : moo.isString(c) ? a[c].apply(a, b) : null
}
var _toString = function(c, b) {
        void 0 === b && (b = 0);
        b || (b = 0);
        for (var a = "",
                 d = 0; d < b; d++) a += "\t";
        var a = a + "{\n",
            g;
        for (g in c) {
            for (d = 0; d <= b; d++) a += "\t";
            a = "object" == typeof c[g] ? a + (g + ":\n" + _toString(c[g], b + 1)) : a + (g + ":" + c[g] + "\n")
        }
        for (d = 0; d < b; d++) a += "\t";
        return a + "}\n"
    },
    isEmpty = function(c) {
        return null == c || void 0 == c || "" == c || 0 == c || -1 == c ? !0 : "number" == typeof c ? isNaN(c) : !1
    },
    timeToHour = function(c) {
        return c / 36E5
    },
    timeToSecond = function(c) {
        return c / 6E4
    },
    deepCopy = function(c, b) {
        for (var a in b) {
            var d = b[a];
            c !== d && (c[a] = "object" === typeof d ? deepCopy(c[a] || {},
                d) : d)
        }
        return c
    };
Function.prototype.bind || (Function.prototype.bind = function(c) {
    if ("function" !== typeof this) throw new TypeError("Function.prototype.bind - what is trying to be bound is not callable");
    var b = Array.prototype.slice.call(arguments, 1),
        a = this,
        d = function() {},
        g = function() {
            return a.apply(this instanceof d && c ? this: c, b.concat(Array.prototype.slice.call(arguments)))
        };
    d.prototype = this.prototype;
    g.prototype = new d;
    return g
});
var HeroModel = function() {
    function c(b) {
        this.configId = 0;
        this.config = null;
        this._need = 0;
        this.autoTimeID = -1;
        this._lv = 1;
        this._speed = 1E3;
        this._skillList = [];
        this._critPro = 0;
        this._critHurt = 10;
        this.extraHurt = 1;
        this.tapCoin = 0;
        this.init(b)
    }
    c.prototype.init = function(b) {
        this.configId = b;
        this.config = RES.getRes("heroConfig")[b];
        this._lv = 0;
        this.cdChange();
        addAction(Const.REBORN_SKILL_REFRESH, this.cdChange, this)
    };
    c.prototype.cdChange = function() {
        this._need = this.configId % 1E3 * 3600;
        var b = applyFilters(Const.GET_HOLY_EFFECT, getApplyString(28));
        b != getApplyString(28) && (this._need *= 1 - b, -1 < this.autoTimeID && (Time.clearTimeout(this.autoTimeID), this.isDead() ? this.autoReborn() : this.reborn()))
    };
    c.prototype.getDes = function() {
        return this.config.heroIntroduction
    };
    c.prototype.getRes = function() {
        return "icon_head_" + this.config.iconID
    };
    c.prototype.isMainHero = function() {
        return 1 == this.config.isMain
    };
    Object.defineProperty(c.prototype, "lv", {
        get: function() {
            return this._lv
        },
        set: function(b) {
            this._lv = b
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "speed", {
        get: function() {
            return this._speed
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "dps", {
        get: function() {
            return this._dps
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "bossDps", {
        get: function() {
            return this._bossDPS
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "nextDPS", {
        get: function() {
            return this._nextDPS
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "tapDamage", {
        get: function() {
            return this._dps
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "nextTapDamage", {
        get: function() {
            return this._nextDPS
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "skillList", {
        get: function() {
            return this._skillList
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "nextSkill", {
        get: function() {
            return this._nextSkill
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "critPro", {
        get: function() {
            return this._critPro
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "critHurt", {
        get: function() {
            return this._critHurt
        },
        enumerable: !0,
        configurable: !0
    });
    c.prototype.changeCritPro = function(b) {
        return this._critPro += b
    };
    c.prototype.canLevelUp = function(b) {
        return 1E3 >= this._lv ? 1E3 >= this._lv + b: !0
    };
    c.prototype.isEvolution = function() {
        return 1E3 < this._lv
    };
    c.prototype.getSkillOpenLv = function() {
        return this.isEvolution() ? this._lv - 1E3: this._lv
    };
    c.prototype.isDead = function() {
        return 0 < this.getRebornTime()
    };
    c.prototype.autoReborn = function() {
        var b = DataCenter.getInstance().totalData.heroList[this.configId].dieTime,
            a = (new Date).getTime();
        this.autoTimeID = Time.setTimeout(this.reborn, this, 1E3 * (this._need - Math.floor((a - b) / 1E3)))
    };
    c.prototype.killMe = function() {
        var b = (new Date).getTime();
        DataCenter.getInstance().totalData.heroList[this.configId].dieTime = b;
        this.autoTimeID = Time.setTimeout(this.reborn, this, 1E3 * this._need)
    };
    c.prototype.reborn = function() {
        Time.clearTimeout(this.autoTimeID);
        this.autoTimeID = -1;
        DataCenter.getInstance().totalData.heroList[this.configId].dieTime = 0;
        this.refreshDataWithSkill();
        SkillController.getInstance().refreshSkillList();
        HeroController.getInstance().refreshHeroModel();
        doAction(Const.REBORN_HERO, this);
        doAction(Const.REFRESH_UNOPEN_LIST)
    };
    c.prototype.getRebornTime = function() {
        if (!DataCenter.getInstance().totalData.heroList[this.configId]) return 0;
        var b = DataCenter.getInstance().totalData.heroList[this.configId].dieTime;
        if (0 == b) return 0;
        var a = (new Date).getTime(),
            b = Math.floor((a - b) / 1E3);
        return b > this._need ? DataCenter.getInstance().totalData.heroList[this.configId].dieTime = 0 : this._need - b
    };
    c.prototype.refreshHeroSkillList = function() {
        this._skillList = [];
        if (this.isMainHero()) {
            var b = DataCenter.getInstance().totalData.mainHero.skill,
                a;
            for (a in b) {
                var d = SkillModel.createByConfigId(a);
                d.lv = b[a];
                this._skillList.push(d)
            }
        } else {
            b = DataCenter.getInstance().totalData.heroList;
            for (a in b) if (a == this.configId) {
                for (var c = 0; c < b[a].skill.length; c++) d = SkillModel.createByConfigId(b[a].skill[c]),
                    this._skillList.push(d);
                break
            }
            this.refreshNextSkill()
        }
    };
    c.prototype.refreshNextSkill = function() {
        if (this.config.skill) {
            var b = this.config.skill.split(",")[this._skillList.length];
            this._nextSkill = b ? SkillModel.createByConfigId(b) : null
        }
    };
    c.prototype.refreshSpeed = function() {
        this._speed = this.config.speed
    };
    c.prototype.refreshBossDPS = function(b, a) {
        1 < b ? (a.times(b), this._bossDPS = a.getNumer()) : this._bossDPS = this._dps
    };
    c.prototype.refreshDPS = function() {
        var b = SkillController.getInstance().getAdditionByHeroModel(this);
        this._critPro = 0.04;
        this._critHurt = 5;
        for (var a = 1,
                 d = 1,
                 c = 0,
                 e = 0,
                 h = 0; h < b.length; h++) 1 == b[h].getEffectType() && (d *= b[h].getValue()),
            30 == b[h].getEffectType() && (a += b[h].getValue()),
            2 == b[h].getEffectType() && (this._critPro += b[h].getValue()),
            3 == b[h].getEffectType() && (this._critHurt *= b[h].getValue()),
            4 == b[h].getEffectType() && (c += b[h].getValue()),
            6 == b[h].getEffectType() && (e += b[h].getValue());
        0 == e && (e = 1);
        h = SkillController.getInstance().holyEffect;
        for (b = 0; b < h.length; b++) 1 != h[b].type || h[b].targetHero != TargetHeroAll && h[b].targetHero != this.configId || (d *= 1 + h[b].value),
            2 == h[b].type && (this._critPro += h[b].value),
            3 == h[b].type && (this._critHurt *= 1 + h[b].value);
        d *= SkillController.getInstance().holyAdd + 1;
        this.isMainHero() || this.isEvolution() && (d *= 45E6);
        d *= a;
        a = this.config.dps.split(",");
        this._bossDPS = this._nextDPS = this._dps = "0";
        b = new NumberModel;
        if (a.length >= this._lv) for (h = 0; h < this._lv; h++) b.add(a[h]);
        else for (b.add(a[a.length - 1]), b.times(this.config.ratio), b.times((1 - Math.pow(this.config.ratio, this.lv - a.length)) / (1 - this.config.ratio)), h = 0; h < a.length; h++) b.add(a[h]);
        1 < d && b.times(d);
        0 < c && (h = new NumberModel, h.add(HeroController.getInstance().currentDPS), h.times(c), b.add(h.getNumer()));
        this._dps = b.getNumer();
        this.refreshBossDPS(e, b);
        e = new NumberModel;
        if (a.length >= this._lv + 1) {
            for (h = 0; h < this._lv + 1; h++) e.add(a[h]);
            1 < d && e.times(d);
            0 < c && (h = new NumberModel, h.add(HeroController.getInstance().currentDPS), h.times(c), e.add(h.getNumer()));
            e.reduce(this._dps)
        } else e.add(a[a.length - 1]),
            e.times(Math.pow(this.config.ratio, this.lv + 1 - a.length)),
            1 < d && e.times(d);
        this._nextDPS = e.getNumer()
    };
    c.prototype.getCostNum = function() {
        return this.isMainHero() ? [this.levelup1(), this.levelup10(), this.levelup100()] : [this.getCostNumByLevel(this.lv + 1), this.getCostNumByLevel(this.lv + 10), this.getCostNumByLevel(this.lv + 100)]
    };
    c.prototype.levelup1 = function() {
        var b = this.config.callCost.split(","),
            a = new NumberModel;
        b.length > this.lv ? a.add(b[this.lv]) : a.add(b[b.length - 1] * Math.pow(this.config.costRatio, this.lv - 21 + 1));
        return GameUtils.getCallCost(a.getNumer())
    };
    c.prototype.levelup10 = function() {
        var b = this.config.callCost.split(","),
            a = 0,
            d = new NumberModel;
        if (b.length > this.lv + 10) {
            for (var c = this.lv; c < this.lv + 10; c++) a += parseInt(b[c]);
            d.add(a)
        } else if (this.lv < b.length) {
            for (c = this.lv; c < b.length; c++) a += parseInt(b[c]);
            d.add(b[b.length - 1] * this.config.costRatio);
            d.times((1 - Math.pow(this.config.costRatio, this.lv + 10 - b.length)) / (1 - this.config.costRatio));
            d.add(a)
        } else d.add(b[b.length - 1] * Math.pow(this.config.costRatio, this.lv - b.length + 1)),
            d.times((1 - Math.pow(this.config.costRatio, 10)) / (1 - this.config.costRatio));
        return GameUtils.getCallCost(d.getNumer())
    };
    c.prototype.levelup100 = function() {
        var b = this.config.callCost.split(","),
            a = 0,
            d = new NumberModel;
        if (this.lv < b.length) {
            for (var c = this.lv; c < b.length; c++) a += parseInt(b[c]);
            d.add(b[b.length - 1] * this.config.costRatio);
            d.times((1 - Math.pow(this.config.costRatio, this.lv + 100 - b.length)) / (1 - this.config.costRatio));
            d.add(a)
        } else d.add(b[b.length - 1] * Math.pow(this.config.costRatio, this.lv - b.length + 1)),
            d.times((1 - Math.pow(this.config.costRatio, 100)) / (1 - this.config.costRatio));
        return GameUtils.getCallCost(d.getNumer())
    };
    c.prototype.getCostNumByLevel = function(b) {
        var a = this.config.callCost,
            d = new NumberModel;
        d.add(a);
        d.times(Math.pow(this.config.costRatio, this.lv));
        d.times((1 - Math.pow(this.config.costRatio, b - this.lv)) / (1 - this.config.costRatio));
        return GameUtils.getCallCost(d.getNumer())
    };
    c.prototype.getEvolutionCost = function() {
        var b = this.config.callCost,
            a = new NumberModel;
        a.add(b);
        a.times(Math.pow(this.config.costRatio, 100));
        return GameUtils.getCallCost(a.getNumer())
    };
    c.prototype.refreshDataWithSkill = function() {
        this.isMainHero() || this.refreshSpeed();
        this.refreshDPS()
    };
    c.createByConfigId = function(b) {
        return HeroModelCache.getInstance().getCache(b)
    };
    return c
} ();
HeroModel.prototype.__class__ = "HeroModel";
var HeroModelCache = function() {
    function c() {
        this._models = []
    }
    c.getInstance = function() {
        null == this._instance && (this._instance = new c);
        return this._instance
    };
    c.prototype.getCache = function(b) {
        if (this._models[b]) return this._models[b].refreshHeroSkillList(),
            this._models[b];
        var a = new HeroModel(b);
        a.refreshHeroSkillList();
        return this._models[b] = a
    };
    return c
} ();
HeroModelCache.prototype.__class__ = "HeroModelCache";
var NumberModel = function() {
    function c(b) {
        void 0 === b && (b = null);
        this.units = "i K M B T aa bb cc dd ee ff gg hh ii jj kk ll mm nn oo pp qq rr ss tt uu vv ww xx yy zz".split(" ");
        this.dataNumber = 0;
        this.splitNumber = 9.999999999999999E92;
        b && this.add(b)
    }
    c.prototype.add = function(b) {
        if (!b) return ! 1;
        if (isNaN(b)) {
            if ("-" == b.substr(0, 1)) return this.reduceString(b.substr(1));
            this.addString(b);
            return ! 0
        }
        b = Number(b);
        if (0 > b) return this.reduceInt(Math.abs(b));
        this.addInt(b);
        return ! 0
    };
    c.prototype.reduce = function(b) {
        return isNaN(b) ? this.reduceString(b) : this.reduceInt(Number(b))
    };
    c.prototype.times = function(b, a) {
        void 0 === a && (a = "set");
        var d = 0;
        "set" == a ? d = this.dataNumber *= b: (d = this.dataNumber, d *= b);
        return this.getNumer(d)
    };
    c.prototype.percent = function(b) {
        return Math.floor(this.dataNumber / b * 100)
    };
    c.prototype.getNumer = function(b) {
        void 0 === b && (b = null);
        b = b ? b: this.dataNumber;
        return b >= this.splitNumber ? this.bigDataFormat(b) : this.littleDataFormat(b)
    };
    c.prototype.compare = function(b) {
        var a = 0,
            a = isNaN(b) ? this.stringToNumber(b) : Number(b);
        return this.dataNumber >= a ? !0 : !1
    };
    c.prototype.pieces = function(b) {
        for (var a = [], d = Math.floor(this.dataNumber / b), c = this.dataNumber - d * b, e = 0; e < b; e++) e == b - 1 ? a.push(this.getNumer(d + c)) : a.push(this.getNumer(d));
        return a
    };
    c.prototype.trace = function(b) {
        this.add(b);
        return this.getNumer()
    };
    c.prototype.setData = function(b) {
        this.dataNumber = Number(b)
    };
    c.prototype.getData = function() {
        return this.dataNumber.toString()
    };
    c.prototype.getDataNum = function() {
        return this.dataNumber
    };
    c.prototype.bigDataFormat = function(b) {
        b = b.toString();
        for (var a = 0,
                 d = 0; d < b.length; d++) if ("e" == b[d] || "E" == b[d]) {
            a = d;
            break
        }
        d = Math.floor(100 * Number(b.substr(0, a))) / 100;
        b = b.substr(a);
        return d.toFixed(2) + b
    };
    c.prototype.littleDataFormat = function(b) {
        b = this.intToUnits(b);
        var a = "",
            d;
        for (d in b) a = d;
        if ("i" == a) return Math.floor(b[a]).toString();
        d = this.getLittleUnit(a);
        return (Math.floor(100 * (b[a] + b[d] / 1E3)) / 100).toFixed(2) + a
    };
    c.prototype.getLittleUnit = function(b) {
        var a = "",
            d;
        for (d in this.units) {
            if (this.units[d] == b) return a;
            a = this.units[d]
        }
    };
    c.prototype.addInt = function(b) {
        this.dataNumber += b
    };
    c.prototype.addString = function(b) {
        b = this.stringToNumber(b);
        this.dataNumber += b;
        return ! 0
    };
    c.prototype.stringToNumber = function(b) {
        for (var a = 0,
                 d = /[0-9.]+/,
                 c = 0; c < b.length; c++) if (!d.test(b[c])) {
            a = c;
            break
        }
        d = Number(b.substr(0, a));
        b = b.substr(a);
        return this.unitIn(b) ? this.unitToNumber(b, d) : 0
    };
    c.prototype.unitIn = function(b) {
        var a = !1,
            d;
        for (d in this.units) if (this.units[d] == b) {
            a = !0;
            break
        }
        return a
    };
    c.prototype.unitToNumber = function(b, a) {
        for (var d in this.units) {
            if (this.units[d] == b) break;
            a *= 1E3
        }
        return a
    };
    c.prototype.reduceInt = function(b) {
        if (b > this.dataNumber) return ! 1;
        this.dataNumber -= b;
        return ! 0
    };
    c.prototype.reduceString = function(b) {
        b = this.stringToNumber(b);
        if (b > this.dataNumber) return ! 1;
        this.dataNumber -= b;
        return ! 0
    };
    c.prototype.intToUnits = function(b) {
        var a = 0,
            d = {},
            c;
        for (c in this.units) if (a = b % 1E3, b = Math.floor(b / 1E3), d[this.units[c]] = a, 0 == b) break;
        return d
    };
    return c
} ();
NumberModel.prototype.__class__ = "NumberModel";
var __moo_filter_id = 0,
    __moo_filter = {},
    __moo_actions = null,
    __moo_current_filter = [],
    __event_context = this,
    __functionContextIndex = 1,
    __functionIndex = 1,
    __moo_objects = {};
function _mooFilterBuildUniqueId(c, b, a, d) {
    a.__event__id__ || (a.__event__id__ = __functionContextIndex++);
    "string" == typeof b && (b = a[b]);
    b.__event__id__ || (b.__event__id__ = __functionIndex++);
    return a.__event__id__ + "_" + c + "_" + b.__event__id__
}
function _mooCallAllHook(c, b) {
    var a = [];
    if (0 == a.length) for (var d = 0; d < arguments.length; d++) a.push(arguments[d]);
    for (var g in __moo_filter.all) {
        var d = __moo_filter.all[g],
            e;
        for (e in d) {
            var h = d[e];
            h["function"] && (a[1] = b, b = callUserFunction(h["function"], a, h.context))
        }
    }
    return b
}
function addAction(c, b, a, d) {
    void 0 === d && (d = 100);
    return addFilter(c, b, a, d)
}
function addFilter(c, b, a, d) {
    void 0 === d && (d = 100);
    var g = _mooFilterBuildUniqueId(c, b, a, d);
    __moo_filter[c] || (__moo_filter[c] = {});
    __moo_filter[c][d] || (__moo_filter[c][d] = {});
    __moo_filter[c][d][g] = {
        "function": b,
        context: a
    };
    __moo_objects[a.__event__id__] || (__moo_objects[a.__event__id__] = []);
    __moo_objects[a.__event__id__].push({
        idx: g,
        priority: d
    });
    return ! 0
}
function doAction(c) {
    for (var b = [], a = 1; a < arguments.length; a++) b[a - 1] = arguments[a];
    __moo_actions || (__moo_actions = []);
    __moo_actions[c] ? __moo_actions[c] = 1 : __moo_actions[c]++;
    __moo_actions.all && (__moo_current_filter.push(c), _mooCallAllHook.apply(_mooCallAllHook, b));
    if (__moo_filter[c]) {
        var a = __moo_filter[c],
            d;
        for (d in a) {
            var a = __moo_filter[c][d],
                g;
            for (g in a) {
                var e = a[g];
                e["function"] && callUserFunction(e["function"], b, e.context)
            }
        }
        __moo_current_filter.pop()
    } else __moo_filter.all && __moo_current_filter.pop()
}
function didAction(c) {
    return __moo_actions && __moo_actions[c] ? __moo_actions[c] : !1
}
function applyFilters(c) {
    for (var b = [], a = 1; a < arguments.length; a++) b[a - 1] = arguments[a];
    var a = [],
        d = b[0],
        g = d;
    if (__moo_filter.all) {
        a = [c];
        __moo_current_filter.push(c);
        for (g = 0; g < b.length; g++) a.push(b[g]);
        g = _mooCallAllHook.apply(_mooCallAllHook, a)
    }
    if (!__moo_filter[c]) return __moo_filter.all && __moo_current_filter.pop(),
        g;
    __moo_filter.all || __moo_current_filter.push(c);
    if (0 == a.length) for (a = [c], g = 0; g < b.length; g++) a.push(b[g]);
    for (var e in __moo_filter[c]) {
        var b = __moo_filter[c][e],
            h;
        for (h in b) g = b[h],
            g["function"] && (a[1] = d, g = callUserFunction(g["function"], a.slice(1, a.length), g.context), "undefined" != typeof g && (d = g))
    }
    __moo_current_filter.pop();
    return d
}
function removeFilter(c, b, a, d) {
    d = formatFunctionParam(d, 100);
    a = formatFunctionParam(a, __event_context);
    b = _mooFilterBuildUniqueId(c, b, a, d);
    return removeFilterByBase(c, d, b, a)
}
function removeFilterByBase(c, b, a, d) {
    var g = __moo_filter[c] && __moo_filter[c][b] && __moo_filter[c][b][a];
    if (g) for (delete __moo_filter[c][b][a], moo.isEmptyTemp(__moo_filter[c][b]) && delete __moo_filter[c][b], c = __moo_objects[d.__event__id__], d = c.length - 1; 0 <= d; d--) {
        var e = c[d];
        e.idx == a && e.priority == b && c.splice(d, 1)
    }
    return g
}
function removeAllFilters(c, b) {
    b = formatFunctionParam(b, !1);
    __moo_filter[c] && (!1 !== b && __moo_filter[c][b] ? delete __moo_filter[c][b] : delete __moo_filter[c]);
    return ! 0
}
function hasAction(c, b) {
    return hasFilter(c, b, null)
}
function hasFilter(c, b, a) {
    a = formatFunctionParam(a, __event_context);
    b = formatFunctionParam(b, !1);
    var d = moo.isEmpty(__moo_filter[c]);
    if (!1 === b || !1 == d) return d;
    b = _mooFilterBuildUniqueId(c, b, a, !1);
    if (!b) return ! 1;
    for (var g in __moo_filter[c]) if (g = parseInt(g), __moo_filter[c][g][b]) return g;
    return ! 1
}
function removeAction(c, b, a, d) {
    return removeFilter(c, b, a, d)
}
function removeAllAction(c, b) {
    return removeAllFilters(c, b)
}
function removeEventsByTarget(c) {
    for (var b = c.__event__id__,
             a = __moo_objects[b], d = /^\d+\_(.*?)\_\d+$/gi; a && a.length;) {
        var g = a[0],
            e = d.exec(g.idx);
        e && removeFilterByBase(e[1], g.priority, g.idx, c)
    }
    delete __moo_objects[b]
}
var SkillController = function() {
    function c() {
        this._mainSkillList = [];
        this._skillList = [];
        this._holySkillList = [];
        this.holyEffect = [];
        this.holyAdd = 0
    }
    c.getInstance = function() {
        null == this._instance && (this._instance = new c);
        return this._instance
    };
    c.prototype.refreshSkillList = function() {
        this._skillList = [];
        for (var b = HeroController.getInstance().getHeroList(), a = 0; a < b.length; a++) if (!b[a].isDead()) for (var d = b[a].skillList, c = 0; c < d.length; c++) this._skillList.push(d[c]);
        this.boxDropCoinTimes = this.dropCoinTimes = 1;
        b = this._skillList.length;
        for (a = 0; a < b; a++) 5 == this._skillList[a].getEffectType() && (this.dropCoinTimes *= this._skillList[a].config.value),
            7 == this._skillList[a].getEffectType() && (this.boxDropCoinTimes *= this._skillList[a].config.value);
        this.refreshMainSkill()
    };
    c.prototype.refreshHolySkillList = function() {
        this._holySkillList = [];
        this.holyEffect = [];
        this.holyAdd = 0;
        this.dropCoinTimes1 = 1;
        for (var b = DataCenter.getInstance().totalData.rebornSkillList, a = 0; a < b.length; a++) {
            var d = RebornSkillModel.createByConfigId(b[a].id);
            d.lv = b[a].lv;
            d.callCost = b[a].cost;
            d.bindCost = b[a].bind;
            d.refreshEffect();
            this._holySkillList.push(d)
        }
    };
    c.prototype.addHolyEffect = function(b, a, d) {
        var c = {};
        c.type = b;
        c.value = d;
        c.targetHero = a;
        5 == b && (this.dropCoinTimes1 += d);
        this.holyEffect.push(c)
    };
    c.prototype.addHolyAdd = function(b) {
        this.holyAdd += b
    };
    c.prototype.getHolySkillList = function() {
        return this._holySkillList
    };
    c.prototype.getSkillList = function() {
        return this._skillList
    };
    c.prototype.refreshMainSkill = function() {
        this._mainSkillList = [];
        for (var b = HeroController.getInstance().mainHero, a = 0; a < b.skillList.length; a++) this._mainSkillList.push(b.skillList[a])
    };
    c.prototype.getAdditionByHeroModel = function(b) {
        for (var a = [], d = 0; d < this._skillList.length; d++) {
            var c = this._skillList[d].getTargetHero();
            c != TargetHeroAll && c != b.configId || a.push(this._skillList[d])
        }
        return a
    };
    return c
} ();
SkillController.prototype.__class__ = "SkillController";
var StatisticsType = function() {
    function c() {}
    c.enemy = "enemy";
    c.coin = "coin";
    c.level = "level";
    c.skull = "skull";
    c.halidome = "halidome";
    c.dps = "dps";
    c.boss = "boss";
    c.tap = "tap";
    c.metempsychosis = "metempsychosis";
    c.upgradehero = "upgradehero";
    c.openbox = "openbox";
    c.gift = "gift";
    c.jumpattack = "jumpattack";
    c.crit = "crit";
    c.rebornTime = "rebornTime";
    c.maxLv = "maxLv";
    c.rechargeHolyNum = "rechargeHolyNum";
    c.loseSc = "loseSc";
    c.loseDPS = "loseDPS";
    c.startGameTime = "startGameTime";
    c.onLineTime = "onLineTime";
    return c
} ();
StatisticsType.prototype.__class__ = "StatisticsType";
var StatisticsModel = function() {
    function c(b) {
        this._notifyName = null;
        this._data = b
    }
    Object.defineProperty(c.prototype, "notifyName", {
        get: function() {
            return this._notifyName
        },
        set: function(b) {
            this._notifyName = b
        },
        enumerable: !0,
        configurable: !0
    });
    c.prototype.resetValue = function(b, a) {
        delete this._data[b];
        this.addValue(b, a)
    };
    c.prototype.updateValue = function(b, a) {
        this._data.hasOwnProperty(b) || (this._data[b] = "0");
        var d = new NumberModel;
        d.add(this._data[b]);
        d.compare(a) || (d = new NumberModel, d.add(a), this._data[b] = d.getNumer())
    };
    c.prototype.addTime = function(b) {
        this._data.hasOwnProperty(b) || (this._data[b] = 0);
        this._data[b] += 1
    };
    c.prototype.resetData = function(b, a) {
        this._data[b] = a
    };
    c.prototype.getDataByType = function(b) {
        return this._data[b] || 0
    };
    c.prototype.addValue = function(b, a) {
        void 0 === a && (a = "1");
        this._data.hasOwnProperty(b) || (this._data[b] = "0");
        var d = new NumberModel;
        d.add(this._data[b]);
        d.add(a);
        this._data[b] = d.getNumer();
        null != this._notifyName && doAction(this._notifyName)
    };
    c.prototype.removeValue = function(b, a) {
        void 0 === a && (a = "1");
        this._data.hasOwnProperty(b) || (this._data[b] = "0");
        var d = new NumberModel;
        d.add(this._data[b]);
        d.reduce(a);
        this._data[b] = d.getNumer();
        null != this._notifyName && doAction(this._notifyName)
    };
    c.prototype.getValue = function(b) {
        return this._data.hasOwnProperty(b) ? this._data[b] : "0"
    };
    c.prototype.reset = function(b) {
        void 0 === b && (b = {});
        this._data = b
    };
    c.prototype.getData = function() {
        return this._data
    };
    return c
} ();
StatisticsModel.prototype.__class__ = "StatisticsModel";
var Const = function() {
    function c() {}
    c.REFRESH_BOSS = "refresh_boss";
    c.REFRESH_HOME_BG = "refresh_home";
    c.ADD_HERO = "add_hero";
    c.REBORN_HERO = "reborn_hero";
    c.MAIN_HERO_SKILL_CHANGE = "main_hero_skill_change";
    c.HERO_LIST_CHANGE = "hero_list_change";
    c.CHANGE_DIAMOND = "change_diamond";
    c.CHANGE_DEFAULT_SHARE = "CHANGE_DEFAULT_SHARE";
    c.ADD_DIAMOND = "add_diamond";
    c.CHANGE_SPEED = "change_speed";
    c.CLOSE_SHARE_TIP = "close_share_tip";
    c.AWARD_BOX_TIP = "award_box_tip";
    c.AWARD_EFFECT = "award_effect";
    c.START_GUIDE = "start_guide";
    c.SHOW_AD_DONE = "show_ad_done";
    c.SHOW_MAIN_LAYER = "show_main_layer";
    c.DROP_COIN = "drop_coin";
    c.GET_ALL_COIN = "get_all_coin";
    c.FLY_COIN = "fly_coin";
    c.HIDE_GOLDCOIN = "hide_goldcoin";
    c.HIDE_HOLYCOIN = "hide_holycoin";
    c.SHOW_HOLYCOIN = "add_holycoin";
    c.LEAVE_BATTLE = "leave_battle";
    c.CLICK_CD_SKILL = "click_cd_skill";
    c.FIGHT_BOSS = "fight_boss";
    c.ACHIEVE = "achieve";
    c.BOSS_KILLED = "boss_killed";
    c.CHANGE_STATISTICS = "change_statistics";
    c.HERO_ATTACK = "hero_attack";
    c.CHANGE_COIN = "change_coin";
    c.REFRESH_SKILL_VIEW = "refresh_skill_view";
    c.ISBOSS = "isBoss";
    c.ADD_COIN = "add_coin";
    c.REFRESH_UNOPEN_LIST = "refresh_unopen_list";
    c.BEGIN_MAIN_SKILL_1 = "begin_main_skill_1";
    c.BEGIN_MAIN_SKILL_2 = "begin_main_skill_2";
    c.BEGIN_MAIN_SKILL_3 = "begin_main_skill_3";
    c.BEGIN_MAIN_SKILL_4 = "begin_main_skill_4";
    c.BEGIN_MAIN_SKILL_5 = "begin_main_skill_5";
    c.STOP_MAIN_SKILL_1 = "stop_main_skill_1";
    c.STOP_MAIN_SKILL_2 = "stop_main_skill_2";
    c.STOP_MAIN_SKILL_3 = "stop_main_skill_3";
    c.STOP_MAIN_SKILL_4 = "stop_main_skill_4";
    c.STOP_MAIN_SKILL_5 = "stop_main_skill_5";
    c.MAIN_LAYER = "main_layer";
    c.CHANGE_CLICK_COIN = "change_click_coin";
    c.UNLOCK_MAIN_SKILL = "unlock_main_skill";
    c.LEVELUP_MAIN_SKILL = "levelup_main_skill";
    c.REFRESH_CURRENT_DPS = "refresh_current_dps";
    c.SKILL_LAYER_EXSIT = "skill_layer_exsit";
    c.USE_PAYMENT_SKILL = "use_payment_skill";
    c.GET_PAYMENT_EFFECT = "get_payment_effect";
    c.REFRESH_ACHIEVE = "refresh_achieve";
    c.INFO_INITED = "info_inited";
    c.UNLOCK_LEVEL = "unlock_level";
    c.SHOWBOXDESC = "show_openbox_desc";
    c.STOP_MAIN_SKILL_VALUE_2 = "stop_main_skill_value_2";
    c.CHANGE_HOLY_NUM = "change_holy_num";
    c.CHANGE_REBORN_SKILL = "change_reborn_skill";
    c.RESET_GAME = "reset_game";
    c.REBORN_SKILL_REFRESH = "reborn_skill_refresh";
    c.GET_HOLY_EFFECT = "get_holy_effect";
    c.HERO_DEAD = "hero_dead";
    c.OPEN_BOX = "open_box";
    c.GET_NEW_HERO = "new_hero";
    c.UPDATE_HERO = "update_hero";
    c.LOCK_SKILL = "lock_skill";
    c.UNLOCK_SKILL = "unlock_skill";
    c.IS_SKILL_USING = "is_skill_using";
    c.CLEAN_SKILL_CD = "clean_skill_cd";
    c.IS_BOX_SKILL_USING = "is_box_skill_using";
    c.BOX_SKILL_OVER = "box_skill_over";
    c.REDUCE_DIAMOND = "reduce_diamond";
    c.CHANGE_JIFEN = "change_jifen";
    c.SHOW_TIPS = "show_tips";
    c.HERO_UPGRADE = "hero_upgrade";
    c.ENTER_FIGHT = "enter_fight";
    c.maxLevel = 2E4;
    c.maxRebornSkill = 29;
    c.maxHero = 30;
    return c
} ();
Const.prototype.__class__ = "Const";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    GameMainLayer = function(c) {
        function b() {
            c.call(this);
            this._achieveLayer = null;
            b.instance = this;
            this.init()
        }
        __extends(b, c);
        b.prototype.startGuide = function() { (new GuideManager).execute(DataCenter.getInstance().totalData.guideStep)
        };
        b.prototype.init = function() {
            this.gameLayer = new GameLayer;
            this.addChild(this.gameLayer);
            this.bottomLayer = new BottomLayer;
            this.addChild(this.bottomLayer);
            this.bottomLayer.y = 930;
            this._boxContainer = new egret.DisplayObjectContainer;
            this.addChild(this._boxContainer);
            this._topBoxContainer = new egret.DisplayObjectContainer;
            this.addChild(this._topBoxContainer);
            this._tipsContainer = new egret.DisplayObjectContainer;
            this.addChild(this._tipsContainer);
            egret.MainContext.instance.stage.addEventListener(egret.Event.DEACTIVATE, this.onBlur, this);
            egret.MainContext.instance.stage.addEventListener(egret.Event.ACTIVATE, this.onFocus, this);
            addAction(Const.BOSS_KILLED, this.bossKill, this);
            this.asyncLoading();
            addAction(Const.START_GUIDE, this.startGuide, this);
            doAction(Const.SHOW_MAIN_LAYER);
            addAction(Const.SHOW_TIPS, this.showTips, this)
        };
        b.prototype.showTips = function(a) {
            null == this._tipsLayer && (this._tipsLayer = new TipsContainer, this._tipsLayer.y = 250, this._tipsContainer.addChild(this._tipsLayer));
            this._tipsLayer.showMessage(a)
        };
        b.prototype.asyncLoading = function() {
            GameUtils.preloadAnimation("next_level_json");
            GameUtils.preloadAnimation("hero_skill_1_json");
            GameUtils.preloadAnimation("hero_skill_2_json");
            GameUtils.preloadAnimation("hero_skill_3_json");
            GameUtils.preloadAnimation("hero_skill_4_json");
            GameUtils.preloadAnimation("hero_skill_5_json");
            GameUtils.preloadAnimation("click_json");
            GameUtils.preloadAnimation("hero_upgrade_json")
        };
        b.prototype.bossKill = function() {
            if (RES.isGroupLoaded("group_next_level_json")) {
                var a = DataCenter.getInstance().totalData.currentLevel;
                if (5 < a && 1 == a % 5) {
                    var b = new Movie("next_level_json", "next_level"),
                        c = new egret.TextField,
                        e = GameUtils.getMapConfig();
                    c.text = e.mapName;
                    c.anchorX = c.anchorY = 0.5;
                    c.textColor = 2040361;
                    c.size = 35;
                    b.addBoneDisplay("TitleContainer", c);
                    c = new egret.TextField;
                    c.text = "\u5df2\u89e3\u9501!";
                    c.size = 20;
                    c.anchorX = c.anchorY = 0.5;
                    b.addBoneDisplay("DescContainer", c);
                    b.play("1");
                    b.setCallback(function(b) {
                            b == Movie.PLAY_COMPLETE && doAction(Const.UNLOCK_LEVEL, a)
                        },
                        this);
                    this.addChild(b);
                    b.x = 320;
                    b.y = 480
                }
            }
        };
        b.prototype._initGuideBtn = function() {
            this._redictBtn = new egret.Bitmap;
            this._redictBtn.x = 325;
            this._redictBtn.y = 405;
            this._redictBtn.anchorX = this._redictBtn.anchorY = 0.5;
            this._redictBtn.width = 80;
            this._redictBtn.height = 80;
            this.addChild(this._redictBtn);
            this._guideBtn = new GuideButtonLayer;
            this._guideBtn.updateView(this._redictBtn);
            this.addChild(this._guideBtn)
        };
        b.prototype.showGuide = function(a, b) {
            null == this._guideBtn && this._initGuideBtn();
            this._guideBtn.visible = !0;
            this._redictBtn.x = a;
            this._redictBtn.y = b;
            this._guideBtn.maskAndHand()
        };
        b.prototype.hideGuide = function() {
            this._guideBtn.visible = !1;
            this._redictBtn.visible = !1
        };
        b.prototype.onBlur = function() {
            SoundManager.getInstance().pauseBackMusic()
        };
        b.prototype.onFocus = function() {
            SoundManager.getInstance().resumeBackMusic()
        };
        b.prototype.showSetLayer = function() {
            null == this._setLayer && (this._setLayer = new GameSetLayer, this._boxContainer.addChild(this._setLayer));
            this._setLayer.visible = !0
        };
        b.prototype.setAchieveLayerVisible = function(a) {
            void 0 === a && (a = !0);
            null == this._achieveLayer && (this._achieveLayer = new AchievementLayer, this._boxContainer.addChild(this._achieveLayer));
            a ? (this._achieveLayer.visible = !0, this._achieveLayer.reload()) : this._achieveLayer.visible = !1;
            this.bottomLayer.visible = !a;
            this.gameLayer.visible = !a;
            a && doAction("test")
        };
        b.prototype.showRebornSkillDetailLayer = function(a) {
            null == this._rebornSkillDetailLayer && (this._rebornSkillDetailLayer = new RebornSkillDetailLayer, this._boxContainer.addChild(this._rebornSkillDetailLayer));
            this._rebornSkillDetailLayer.refreshView(a);
            this._rebornSkillDetailLayer.visible = !0
        };
        b.prototype.hideRebornSkillDetailLayer = function() {
            this._rebornSkillDetailLayer.visible = !1
        };
        b.prototype.showHeroDetailLayer = function(a) {
            void 0 === a && (a = null);
            null == this._heroDetailLayer && (this._heroDetailLayer = new HeroDetailView, this._boxContainer.addChild(this._heroDetailLayer), this._heroDetailLayer.x = 18);
            this._heroDetailLayer.refreshView(a);
            this._heroDetailLayer.visible = !0
        };
        b.prototype.hideHeroDetailLayer = function() {
            this._heroDetailLayer.visible = !1
        };
        b.prototype.showMainHeroDetailLayer = function(a) {
            void 0 === a && (a = null);
            null == this._heroMainDetailLayer && (this._heroMainDetailLayer = new MainHeroDetailView, this._boxContainer.addChild(this._heroMainDetailLayer), this._heroMainDetailLayer.x = 18);
            this._heroMainDetailLayer.refreshView(a);
            this._heroMainDetailLayer.visible = !0
        };
        b.prototype.hideMainHeroDetailLayer = function() {
            this._heroMainDetailLayer.visible = !1
        };
        b.prototype.hideSetLayer = function() {
            this._setLayer.visible = !1
        };
        b.prototype.showDieLayer = function(a) {
            null == this._dieLayer && (this._dieLayer = new HeroDieLayer, this._boxContainer.addChild(this._dieLayer));
            this._dieLayer.refreshView(a);
            this._dieLayer.visible = !0
        };
        b.prototype.hideDieLayer = function() {
            this._dieLayer.visible = !1
        };
        b.prototype.showResetGameLayer = function() {
            null == this._resetLayer && (this._resetLayer = new ResetGameConfirmLayer, this._boxContainer.addChild(this._resetLayer));
            this._resetLayer.refreshView();
            this._resetLayer.visible = !0
        };
        b.prototype.hideResetGameLayer = function() {
            this._resetLayer.visible = !1
        };
        b.prototype.showPaymentRecommendLayer = function() {
            null == this._paymentRecommendLayer && (this._paymentRecommendLayer = new PaymentRecommend, this._topBoxContainer.addChild(this._paymentRecommendLayer));
            this._paymentRecommendLayer.refreshView();
            this._paymentRecommendLayer.visible = !0
        };
        b.prototype.hidePaymentRecommendLayer = function() {
            this._paymentRecommendLayer.visible = !1
        };
        b.prototype.showHolyAnimation = function(a) {
            null == this._holyAnimation && (this._holyAnimation = new HolyAnimationLayer, this._boxContainer.addChild(this._holyAnimation));
            this._holyAnimation.refreshAnimation(a);
            this._holyAnimation.visible = !0
        };
        b.prototype.hideHolyAnimation = function() {
            this._holyAnimation.visible = !1
        };
        b.prototype.showTopListLayer = function() {
            null == this._topListLayer && (this._topListLayer = new TopListLayer, this._boxContainer.addChild(this._topListLayer));
            this._topListLayer.reload();
            this._topListLayer.visible = !0
        };
        b.prototype.hideTopListLayer = function() {
            this._topListLayer.visible = !1
        };
        b.prototype.showShareLayer = function(a, b, c) {
            void 0 === b && (b = null);
            void 0 === c && (c = null);
            null == this._shareLayer && (this._shareLayer = new ShareLayer, this._boxContainer.addChild(this._shareLayer));
            this._shareLayer.refreshView(a, b, c);
            this._shareLayer.visible = !0
        };
        b.prototype.hideShareLayer = function() {
            this._shareLayer.visible = !1
        };
        b.prototype.showShareTipLayer = function(a) {
            null == this._shareTipLayer && (this._shareTipLayer = new ShareTipLayer, this._boxContainer.addChild(this._shareTipLayer));
            this._shareTipLayer.show(a);
            this._shareTipLayer.visible = !0
        };
        b.prototype.hideShareTipLayer = function() {
            this._shareTipLayer.visible = !1
        };
        b.prototype.showAdLayer = function(a) {
            null == this._adLayer && (this._adLayer = new AdLayer, this._boxContainer.addChild(this._adLayer));
            this._adLayer.show(a);
            this._adLayer.visible = !0
        };
        b.prototype.hideAdLayer = function() {
            this._adLayer.visible = !1
        };
        b.prototype.showStatisticsLayer = function() {
            null == this._statisticsLayer && (this._statisticsLayer = new StatisticsLayer, this.addChild(this._statisticsLayer));
            this._statisticsLayer.refreshView();
            this._statisticsLayer.visible = !0
        };
        b.prototype.hideStatisticsLayer = function() {
            this._statisticsLayer.visible = !1
        };
        b.prototype.showSkillCDLayer = function(a) {
            null == this._mainSkillCDLayer && (this._mainSkillCDLayer = new MainSkillCDLayer, this._boxContainer.addChild(this._mainSkillCDLayer));
            this._mainSkillCDLayer.refreshView(a);
            this._mainSkillCDLayer.visible = !0
        };
        b.prototype.hideSkillCDLayer = function() {
            this._mainSkillCDLayer.visible = !1
        };
        b.prototype.showCodeAwardLayer = function() {
            null == this._codeLayer && (this._codeLayer = new CodeAwardLayer, this._boxContainer.addChild(this._codeLayer));
            this._codeLayer.visible = !0
        };
        b.prototype.hideCodeAwardLayer = function() {
            this._codeLayer.visible = !1
        };
        b.prototype.showSupriseLayer = function() {
            null == this._supriseLayer && (this._supriseLayer = new SupriseLayer, this._boxContainer.addChild(this._supriseLayer));
            this._supriseLayer.visible = !0;
            this._supriseLayer.refreshView("")
        };
        b.prototype.hideSupriseLayer = function() {
            this._supriseLayer.visible = !1
        };
        b.prototype.showMaskLayer = function() {
            null == this._maskLayer && (this._maskLayer = new MaskLayer, this._topBoxContainer.addChild(this._maskLayer))
        };
        return b
    } (BaseContainer);
GameMainLayer.prototype.__class__ = "GameMainLayer";
var HeroController = function() {
    function c() {
        this._heroList = [];
        this._pieces = {}
    }
    c.getInstance = function() {
        null == this._instance && (this._instance = new c);
        return this._instance
    };
    c.prototype.getHeroList = function() {
        return this._heroList
    };
    c.prototype.getAliveHero = function() {
        for (var b = [], a = 0; a < this._heroList.length; a++) this._heroList[a].isDead() || b.push(this._heroList[a]);
        return b
    };
    c.prototype.getPieceData = function(b) {
        return this._pieces[b] || []
    };
    c.prototype.cleanHeroList = function() {
        for (var b = 0; b < this._heroList.length; b++) this._heroList[b].lv = 0,
            this._heroList[b].refreshDataWithSkill()
    };
    c.prototype.initHeroList = function() {
        this._heroList = [];
        var b = DataCenter.getInstance().totalData.heroList,
            a;
        for (a in b) {
            var d = HeroModel.createByConfigId(a);
            d.lv = b[a].lv;
            d.isDead() && d.autoReborn();
            this._heroList.push(d)
        }
        this._pieces = {};
        for (b = 0; b < this._heroList.length; b++) a = this._heroList[b],
            this._pieces.hasOwnProperty(a.config.timePiece) || (this._pieces[a.config.timePiece] = []),
            this._pieces[a.config.timePiece].push(a);
        this.mainHero = HeroModel.createByConfigId(1E3);
        this.mainHero.lv = DataCenter.getInstance().totalData.mainHero.lv
    };
    c.prototype.addHero = function(b) {
        this._heroList.push(b);
        this._pieces.hasOwnProperty(b.config.timePiece) || (this._pieces[b.config.timePiece] = []);
        this._pieces[b.config.timePiece].push(b)
    };
    c.prototype.refreshHeroModel = function() {
        for (var b = new NumberModel,
                 a = new NumberModel,
                 d = 0; d < this._heroList.length; d++) {
            var c = this._heroList[d];
            c.refreshDataWithSkill();
            c.isDead() || (b.add(c.dps), a.add(c.bossDps))
        }
        this.currentDPS = b.getNumer();
        this.currentBossDPS = a.getNumer();
        this.refreshMainHero();
        doAction(Const.REFRESH_SKILL_VIEW)
    };
    c.prototype.refreshCurrentDPS = function() {
        for (var b = new NumberModel,
                 a = new NumberModel,
                 d = 0; d < this._heroList.length; d++) {
            var c = this._heroList[d];
            c.isDead() || (b.add(c.dps), a.add(c.bossDps))
        }
        this.currentDPS = b.getNumer();
        this.currentBossDPS = a.getNumer();
        DataCenter.getInstance().statistics.resetValue(StatisticsType.dps, this.currentDPS);
        this.refreshMainHero();
        doAction(Const.REFRESH_SKILL_VIEW)
    };
    c.prototype.refreshMainHero = function() {
        this.mainHero.refreshDataWithSkill()
    };
    return c
} ();
HeroController.prototype.__class__ = "HeroController";
var TargetHeroAll = -1,
    SkillModel = function() {
        function c(b) {
            this.configId = 0;
            this.config = null;
            this._lv = -1;
            this.init(b)
        }
        Object.defineProperty(c.prototype, "lv", {
            get: function() {
                return this._lv
            },
            set: function(b) {
                this._lv = b
            },
            enumerable: !0,
            configurable: !0
        });
        c.prototype.getRes = function() {
            var b = "";
            return b = "icon_skill_" + this.config.iconID
        };
        c.prototype.init = function(b) {
            this.configId = parseInt(b);
            this.config = RES.getRes("skillConfig")[b]
        };
        c.prototype.getMainCost = function() {
            var b = new NumberModel;
            b.add(this.config.baseCost);
            b.times(Math.pow(this.config.cost, this._lv));
            return GameUtils.getCallCost(b.getNumer())
        };
        c.prototype.getTargetHero = function() {
            return this.config.targetHero
        };
        c.prototype.getEffectType = function() {
            return this.config.effectType
        };
        c.prototype.getValue = function() {
            return this.config.value
        };
        c.prototype.getOpenLevel = function() {
            return this.config.level
        };
        c.prototype.getCDTime = function() {
            for (var b = SkillController.getInstance().holyEffect, a = 1, d = 0; d < b.length; d++) b[d].type == this.config.type1 && (a = b[d].value + 1);
            return this.config.cd * a
        };
        c.prototype.getLastTime = function() {
            for (var b = SkillController.getInstance().holyEffect, a = 1, d = 0; d < b.length; d++) b[d].type == this.config.type2 && (a = b[d].value + 1);
            return this.config.last * a
        };
        c.prototype.getDes = function() {
            var b;
            b = 0 == this.lv ? this.config.base + this.config.value: this.config.base + this.lv * this.config.value;
            b *= this.config.showTimes;
            10 < b.toString().length && (b = b.toFixed(2));
            return this.config.des.replace("{0}", b)
        };
        c.createByConfigId = function(b) {
            return SkillModelCache.getInstance().getCache(b)
        };
        return c
    } ();
SkillModel.prototype.__class__ = "SkillModel";
var SkillModelCache = function() {
    function c() {
        this._models = []
    }
    c.getInstance = function() {
        null == this._instance && (this._instance = new c);
        return this._instance
    };
    c.prototype.getCache = function(b) {
        if (this._models[b]) return this._models[b];
        var a = new SkillModel(b);
        return this._models[b] = a
    };
    return c
} ();
SkillModelCache.prototype.__class__ = "SkillModelCache";
var getApplyString = function(c) {
        return "get_holy_effect" + c
    },
    SkillEffectType; (function(c) {
    c[c.hurt = 1] = "hurt";
    c[c.critPro = 2] = "critPro";
    c[c.critHurt = 3] = "critHurt";
    c[c.hurt1 = 4] = "hurt1";
    c[c.dropCoin = 5] = "dropCoin";
    c[c.bossHurt = 6] = "bossHurt";
    c[c.boxCoin = 7] = "boxCoin";
    c[c.skillLastTime6 = 8] = "skillLastTime6";
    c[c.skillCDTime6 = 9] = "skillCDTime6";
    c[c.skillLastTime5 = 10] = "skillLastTime5";
    c[c.skillCDTime5 = 11] = "skillCDTime5";
    c[c.skillLastTime4 = 12] = "skillLastTime4";
    c[c.skillCDTime4 = 13] = "skillCDTime4";
    c[c.skillLastTime3 = 14] = "skillLastTime3";
    c[c.skillCDTime3 = 15] = "skillCDTime3";
    c[c.skillLastTime2 = 16] = "skillLastTime2";
    c[c.skillCDTime2 = 17] = "skillCDTime2";
    c[c.skillCDTime1 = 18] = "skillCDTime1";
    c[c.holyAwardNum = 19] = "holyAwardNum";
    c[c.levelUpCost = 20] = "levelUpCost";
    c[c.boxMonster = 21] = "boxMonster";
    c[c.coinTenTimes = 22] = "coinTenTimes";
    c[c.bossCoin = 23] = "bossCoin";
    c[c.bossHP = 24] = "bossHP";
    c[c.bossTime = 25] = "bossTime";
    c[c.customPassNum = 26] = "customPassNum";
    c[c.dieProb = 27] = "dieProb";
    c[c.heroRebornTime = 28] = "heroRebornTime";
    c[c.offLineAward = 29] = "offLineAward";
    c[c.hurt2 = 30] = "hurt2"
})(SkillEffectType || (SkillEffectType = {}));
var TimeInfo = function() {
    function c(b, a, d, c, e) {
        this._listener = b;
        this._thisObject = a;
        this._oldTime = this._time = d;
        this._isOnce = c;
        this._params = e
    }
    Object.defineProperty(c.prototype, "isPause", {
        get: function() {
            return this._isPause
        },
        set: function(b) {
            this._isPause = b
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "isOnce", {
        get: function() {
            return this._isOnce
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "leftTime", {
        get: function() {
            return this._time
        },
        enumerable: !0,
        configurable: !0
    });
    c.prototype.tick = function(b) {
        var a = !0;
        this._time && (this._time -= b, a = 0 >= this._time);
        a && (b = [].concat(this._params, b), this._listener.apply(this._thisObject, b), this._time = this._oldTime);
        return a
    };
    return c
} ();
TimeInfo.prototype.__class__ = "TimeInfo";
var Time = function() {
    function c() {}
    c.setInterval = function(b, a, d) {
        for (var c = [], e = 3; e < arguments.length; e++) c[e - 3] = arguments[e];
        c = new TimeInfo(b, a, d, !1, c);
        return this.setTime(c)
    };
    c.clearInterval = function(b) {
        delete c.timeObject[b]
    };
    c.setTimeout = function(b, a, d) {
        for (var c = [], e = 3; e < arguments.length; e++) c[e - 3] = arguments[e];
        c = new TimeInfo(b, a, d, !0, c);
        return this.setTime(c)
    };
    c.getLeftTime = function(b) {
        return (b = c.timeObject[b]) ? b.leftTime: 0
    };
    c.pauseInterval = function(b) {
        if (b = c.timeObject[b]) b.isPause = !0
    };
    c.resumeInterval = function(b) {
        if (b = c.timeObject[b]) b.isPause = !1
    };
    c.clearTimeout = function(b) {
        delete c.timeObject[b]
    };
    c.setTime = function(b) {
        0 == c.timeId && egret.Ticker.getInstance().register(c.tick, null);
        c.timeId++;
        c.timeObject[c.timeId] = b;
        return c.timeId
    };
    c.tick = function(b) {
        for (var a in c.timeObject) {
            var d = c.timeObject[a]; ! d.isPause && d.tick(b) && d.isOnce && delete c.timeObject[a]
        }
    };
    c.timeObject = {};
    c.timeId = 0;
    return c
} ();
Time.prototype.__class__ = "Time";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    RebornItemView = function(c) {
        function b() {
            c.call(this);
            this.initView()
        }
        __extends(b, c);
        b.prototype.initView = function() {
            this._bg = ResourceUtils.createBitmapFromSheet("hero_item_bg", "commonRes");
            this.addChild(this._bg);
            this._normalContainer = new egret.DisplayObjectContainer;
            this.addChild(this._normalContainer);
            this._specialContainer = new egret.DisplayObjectContainer;
            this.addChild(this._specialContainer);
            this._button1 = new BuySkillButton;
            this._button1.init("hero_lvup_btn", "hero_lvup_disable", "holy_icon");
            this._button1.x = 523;
            this._button1.y = 70;
            this._button1.useZoomOut(!0);
            this._button1.changeScale(1.01);
            this._button1.addOnClick(this.refreshSkill, this);
            this._specialContainer.addChild(this._button1);
            this._touchArea = new egret.Sprite;
            this._touchArea.width = 430;
            this._touchArea.height = 100;
            this._touchArea.touchEnabled = !0;
            this._normalContainer.addChild(this._touchArea);
            this._touchArea.addEventListener(egret.TouchEvent.TOUCH_TAP, this.itemClick, this);
            this._iconContainer = new egret.DisplayObjectContainer;
            this.addChild(this._iconContainer);
            this._staticContainer = new egret.DisplayObjectContainer;
            this._normalContainer.addChild(this._staticContainer);
            this._staticContainer.y = -8;
            this._staticContainer.x = 15;
            var a = new egret.TextField;
            this._staticContainer.addChild(a);
            a.x = 360;
            a.y = 15;
            a.size = 20;
            a.text = "Lv.";
            a.textColor = 16777215;
            this._lvLabel = new egret.TextField;
            this._staticContainer.addChild(this._lvLabel);
            this._lvLabel.x = 390;
            this._lvLabel.y = 15;
            this._lvLabel.size = 20;
            this._lvLabel.textColor = 16777215;
            this._nameLabel = new egret.TextField;
            this.addChild(this._nameLabel);
            this._nameLabel.x = 107;
            this._nameLabel.y = 10;
            this._nameLabel.size = 20;
            this._nameLabel.textColor = 2296834;
            this._desLabel1 = new egret.TextField;
            this._staticContainer.addChild(this._desLabel1);
            this._desLabel1.x = 90;
            this._desLabel1.y = 50;
            this._desLabel1.size = 20;
            this._desLabel1.textColor = 16777215;
            this._desLabel2 = new egret.TextField;
            this._staticContainer.addChild(this._desLabel2);
            this._desLabel2.x = 90;
            this._desLabel2.y = 75;
            this._desLabel2.size = 20;
            this._desLabel2.textColor = 16777215;
            this._staticContainer.cacheAsBitmap = !0;
            this._button = new LevelUpButton2;
            this._button.init("reborn_lvup_btn", "reborn_lvup_disable_btn", "holy_icon");
            this._button.x = 523;
            this._button.y = 69;
            this._normalContainer.addChild(this._button);
            this._button.addOnClick(this.onClickHandler, this);
            this._button.useZoomOut(!0);
            addAction(Const.CHANGE_HOLY_NUM, this.refreshData, this)
        };
        b.prototype.refreshSkill = function() {
            if (this._button1._clickAble) {
                doAction(Guide.BUY_HOLY);
                var a = DataCenter.getInstance().totalData.rebornSkillList.length;
                DataCenter.getInstance().unlockRebornSkill(holyCostNum[a], bindHolyGc[a])
            } else doAction(Const.SHOW_TIPS, "80\u5173\u540e\u6709\u51e0\u7387\u83b7\u5f97")
        };
        b.prototype.onClickHandler = function() {
            DataCenter.getInstance().changeHolyNum( - this._data.getCostNum());
            DataCenter.getInstance().levelUpRebornSkill(this._data.configId);
            this.refreshLabel()
        };
        b.prototype.itemClick = function() {
            GameMainLayer.instance.showRebornSkillDetailLayer(this._data)
        };
        b.prototype.refreshLabel = function() {
            if (this._data) {
                this._nameLabel.text = this._data.config.name;
                this._lvLabel.text = this._data.lv.toString();
                this._desLabel1.text = this._data.getDes1();
                this._desLabel2.text = this._data.getDes2();
                var a = this._data.getCostNum(),
                    b = DataCenter.getInstance().totalData.holyNum;
                this._button.setEnabled(b >= a);
                this._button.refreshView(a, "", "\u5347\u7ea7");
                0 == this._data.lvLimit() ? this._button.refreshView(a, "", "\u5347\u7ea7") : this._data.lv >= this._data.lvLimit() ? (this._button.setEnabled(!1), this._button.refreshView(a, "", "\u7b49\u7ea7\u4e0a\u9650")) : this._button.refreshView(a, "", "\u5347\u7ea7")
            }
        };
        b.prototype.refreshData = function() {
            if (0 == this._idx) {
                var a = DataCenter.getInstance().totalData.rebornSkillList.length >= Const.maxRebornSkill,
                    b = DataCenter.getInstance().totalData.holyNum,
                    c = holyCostNum[DataCenter.getInstance().totalData.rebornSkillList.length];
                this._button1.setClickAble(b >= c && !1 == a);
                this._button1.refreshView(c, "", "\u8d2d\u4e70\u5723\u7269");
                this._nameLabel.text = this._data.name;
                this._desLabel1.text = this._data.des
            } else this.refreshLabel()
        };
        b.prototype.refreshView = function(a, b) {
            this._data = a;
            this._idx = b;
            this._specialContainer.visible = 0 == b;
            this._normalContainer.visible = 0 != b;
            this._iconContainer.removeChildren();
            var c;
            c = 0 == this._idx ? ResourceUtils.createBitmapFromSheet("holy_icon_0", "iconRes") : ResourceUtils.createBitmapFromSheet("holy_icon_" + this._data.configId, "iconRes");
            c.x = 25;
            c.y = 12;
            this._iconContainer.addChild(c);
            c = ResourceUtils.createBitmapFromSheet("icon_decorate");
            c.x = 12;
            c.y = 23;
            this._iconContainer.addChild(c);
            this.refreshData()
        };
        return b
    } (BaseContainer);
RebornItemView.prototype.__class__ = "RebornItemView";
var BuySkillButton = function(c) {
    function b() {
        c.call(this);
        this.anchorX = this.anchorY = 0.5
    }
    __extends(b, c);
    b.prototype.setEnabledState = function() {};
    b.prototype.setClickAble = function(a) {
        this._clickAble = a;
        this._normal.visible = this._clickAble;
        this._dis && (this._dis.visible = !this._clickAble)
    };
    b.prototype.init = function(a, b, c) {
        void 0 === c && (c = "coin_icon");
        this._container = new egret.DisplayObjectContainer;
        this._normal = ResourceUtils.createBitmapFromSheet(a, "commonRes");
        this._container.addChild(this._normal);
        this._dis = ResourceUtils.createBitmapFromSheet(b, "commonRes");
        this._dis.visible = !1;
        this._container.addChild(this._dis);
        a = ResourceUtils.createBitmapFromSheet(c);
        this._container.addChild(a);
        a.x = 42;
        a.y = -22;
        a.scaleX = a.scaleY = 0.67;
        this._staticLabel = new egret.TextField;
        this._container.addChild(this._staticLabel);
        this._staticLabel.text = "\u8d2d\u4e70\u5723\u7269";
        this._staticLabel.width = 150;
        this._staticLabel.x = 85;
        this._staticLabel.y = 20;
        this._staticLabel.size = 20;
        this._staticLabel.textColor = 0;
        this._staticLabel.textAlign = egret.HorizontalAlign.CENTER;
        this._staticLabel.anchorX = 0.5;
        this._costLabel = new egret.TextField;
        this._container.addChild(this._costLabel);
        this._costLabel.x = 70;
        this._costLabel.y = -20;
        this._costLabel.size = 20;
        this._costLabel.width = 80;
        this._costLabel.textColor = 16777215;
        this._valueLabel = new egret.TextField;
        this._container.addChild(this._valueLabel);
        this._valueLabel.x = 80;
        this._valueLabel.y = 35;
        this._valueLabel.size = 20;
        this._valueLabel.width = 145;
        this._valueLabel.textAlign = egret.HorizontalAlign.CENTER;
        this._valueLabel.anchorX = 0.5;
        this._valueLabel.textColor = 0;
        this.addChild(this._container);
        this._container.cacheAsBitmap = !0
    };
    b.prototype.refreshView = function(a, b, c) {
        this._costLabel.text = a;
        this._valueLabel.text = b;
        this._staticLabel.text = c
    };
    return b
} (SimpleButton);
BuySkillButton.prototype.__class__ = "BuySkillButton";
var holyCostNum = [1, 3, 7, 13, 22, 36, 57, 88, 134, 201, 298, 439, 643, 934, 1350, 1940, 2790, 3990, 5680, 8080, 11460, 16200, 22870, 32220, 45310, 63620, 89200, 124880, 174610, 243850],
    bindHolyGc = [37, 39, 43, 49, 57, 67, 79, 93, 109, 127, 147, 169, 193, 219, 247, 277, 309, 343, 379, 417, 457, 499, 543, 589, 637, 687, 739, 793, 849, 907],
    changeHolySc = [1, 5, 14, 28, 50, 79, 117, 166, 226, 300, 387, 489, 608, 744, 900, 1075, 1271, 1490, 1732, 2E3, 2293, 2613, 2962, 3340, 3750, 4191, 4665, 5174, 5718, 6300],
    __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    SkillIcon = function(c) {
        function b() {
            c.call(this);
            this._timeID = this._cdTimeBeforeChange = this._cdTime = 0;
            this._status = 1;
            this._lock = !1
        }
        __extends(b, c);
        b.prototype.refreshView = function(a) {
            4 == this._status && 0 < a.lv && (this._skillModel = a, this._status = 1, this.refreshStatus())
        };
        b.prototype.resetIcon = function() {
            this._status = 4;
            this._timeID && Time.clearInterval(this._timeID);
            this.refreshStatus()
        };
        b.prototype.initView = function(a, b) {
            this._skillModel = a;
            this._skillButton = new SimpleButton;
            var c = ResourceUtils.createBitmapFromSheet("skill_icon_" + b);
            this._skillButton.addChild(c);
            this._skillButton.addOnClick(this.onSkillHandler, this);
            this.addChild(this._skillButton);
            this._skillButton.x = 8;
            this._skillButton.y = 3;
            this._skillMask1 = ResourceUtils.createBitmapFromSheet("skill_cd_mask");
            this.addChild(this._skillMask1);
            this._skillMask1.x = 2.8;
            this._skillMask1.y = -2;
            this._skillMask2 = ResourceUtils.createBitmapFromSheet("skill_cd_mask1");
            this.addChild(this._skillMask2);
            this._skillMask2.x = 2.8;
            this._skillMask2.y = -2;
            this._skillMask = ResourceUtils.createBitmapFromSheet("skill_mask", "commonRes");
            this._skillMask.scaleX = 1.2;
            this._skillMask.scaleY = 1.2;
            this.addChild(this._skillMask);
            this._skillMask.x = 2;
            this._skillMask.y = -2;
            this._lockIcon = ResourceUtils.createBitmapFromSheet("lock_icon");
            this.addChild(this._lockIcon);
            this._lockIcon.x = 31;
            this._lockIcon.y = 15;
            this._lockIcon.scaleX = this._lockIcon.scaleY = 0.5;
            this._timeLabel = new egret.TextField;
            this._timeLabel.x = 19;
            this._timeLabel.y = 33;
            this._timeLabel.size = 24;
            this.addChild(this._timeLabel);
            this.initStatus();
            applyFilters(Const.IS_BOX_SKILL_USING, this._skillModel.configId) != this._skillModel.configId && (4 != this._status && (this._lock = !0, this._skillMask2.visible = !0), addAction(Const.BOX_SKILL_OVER, this.initStatus, this));
            addAction(Const.REBORN_SKILL_REFRESH, this.rebornSkillListChange, this);
            addAction(Const.LOCK_SKILL, this.lockSkill, this);
            addAction(Const.UNLOCK_SKILL, this.unlockSkill, this);
            addFilter(Const.IS_SKILL_USING, this.isUsing, this);
            addAction(Const.CLEAN_SKILL_CD, this.cleanCD, this);
            addFilter(Guide.IS_SKILL_CDING, this.isSkillCDing, this)
        };
        b.prototype.isSkillCDing = function(a) {
            if (this._skillModel.configId == a && 3 == this._status) return 1
        };
        b.prototype.initStatus = function() {
            this._lock = !1;
            var a = DataCenter.getInstance().totalData.mainSkillCD[this._skillModel.configId];
            if (a) {
                var b = (new Date).getTime(),
                    a = Math.floor((b - a) / 1E3);
                a > this._skillModel.getCDTime() ? this._status = 1 : (this._cdTimeBeforeChange = this._skillModel.getCDTime(), this._cdTime = this._cdTimeBeforeChange - a, this._status = 3, this.cdHandler(), this._timeID = Time.setInterval(this.cdHandler, this, 1E3))
            } else this._status = 0 < this._skillModel.lv ? 1 : 4;
            this.refreshStatus()
        };
        b.prototype.cleanCD = function(a) {
            a == this._skillModel.configId && (delete DataCenter.getInstance().totalData.mainSkillCD[this._skillModel.configId], 0 < this._skillModel.lv ? (this._status = 1, Time.clearInterval(this._timeID)) : this._status = 4, this.refreshStatus())
        };
        b.prototype.isUsing = function(a) {
            if (a == this._skillModel.configId && 2 == this._status) return ! 1
        };
        b.prototype.lockSkill = function(a) {
            a == this._skillModel.configId && (this._lock = !0, 4 != this._status && (this._skillMask2.visible = !0))
        };
        b.prototype.unlockSkill = function(a) {
            a == this._skillModel.configId && (this._lock = !1, 4 != this._status && (this._skillMask2.visible = 3 == this._status))
        };
        b.prototype.refreshStatus = function() {
            this._timeLabel.visible = 1 != this._status;
            this._lockIcon.visible = 4 == this._status;
            this._skillMask.visible = 4 == this._status || 3 == this._status || 2 == this._status;
            this._skillMask1.visible = 2 == this._status || 1 == this._status;
            this._skillMask2.visible = 3 == this._status
        };
        b.prototype.onSkillHandler = function() {
            this._lock || (1 != this._status ? 3 == this._status && doAction(Const.CLICK_CD_SKILL, this._skillModel.configId) : (this._status = 2, doAction(Guide.USE_SKILL), this.refreshStatus(), DataCenter.getInstance().useSkill(this._skillModel), this._cdTimeBeforeChange = this._cdTime = this._skillModel.getLastTime(), this.cdHandler(), this._timeID = Time.setInterval(this.cdHandler, this, 1E3), DataCenter.getInstance().refreshSkillCD(this._skillModel.configId, (new Date).getTime())))
        };
        b.prototype.rebornSkillListChange = function() {
            var a = -1;
            2 == this._status ? (a = this._skillModel.getLastTime(), a != this._cdTimeBeforeChange && (this._cdTime += a - this._cdTimeBeforeChange, this._cdTimeBeforeChange = a)) : 3 == this._status && (a = this._skillModel.getCDTime(), a != this._cdTimeBeforeChange && (this._cdTime -= this._cdTimeBeforeChange - a, 0 >= this._cdTime && (this._status = 1, this.refreshStatus(), Time.clearInterval(this._timeID))))
        };
        b.prototype.cdHandler = function() {
            this._cdTime -= 1;
            this._timeLabel.text = GameUtils.formatTime(this._cdTime);
            0 >= this._cdTime && (2 == this._status ? (this._status = 3, this.refreshStatus(), this._cdTimeBeforeChange = this._cdTime = this._skillModel.getCDTime(), DataCenter.getInstance().refreshSkillCD(this._skillModel.configId, (new Date).getTime()), this.cdHandler()) : 3 == this._status && (this._status = 1, this.refreshStatus(), Time.clearInterval(this._timeID)))
        };
        b.prototype.dispose = function() {
            c.prototype.dispose.call(this);
            removeEventsByTarget(this);
            this._timeID && Time.clearInterval(this._timeID)
        };
        return b
    } (BaseContainer);
SkillIcon.prototype.__class__ = "SkillIcon";
var MainSkillStatus; (function(c) {
    c[c.canuse = 1] = "canuse";
    c[c.using = 2] = "using";
    c[c.cding = 3] = "cding";
    c[c.locked = 4] = "locked"
})(MainSkillStatus || (MainSkillStatus = {}));
var MainSkill = function() {
    function c(b) {
        this._skillModel = b
    }
    c.prototype.execute = function() {
        this._status = 2;
        this._lastTimeBeforeChange = this._lastTime = this._skillModel.getLastTime();
        this._timeID = Time.setInterval(this.countDown, this, 1E3);
        this._lastValue = this._skillModel.config.base + this._skillModel.config.value * this._skillModel.lv;
        switch (this._skillModel.configId) {
            case 22001:
                doAction(Const.BEGIN_MAIN_SKILL_1, this._lastValue);
                break;
            case 22002:
                doAction(Const.BEGIN_MAIN_SKILL_2, this._lastValue);
                break;
            case 22003:
                doAction(Const.BEGIN_MAIN_SKILL_3);
                HeroController.getInstance().mainHero.changeCritPro(this._lastValue);
                break;
            case 22004:
                doAction(Const.CHANGE_SPEED, this._lastValue + 1);
                break;
            case 22005:
                doAction(Const.BEGIN_MAIN_SKILL_4);
                this._lastValue += 1;
                HeroController.getInstance().mainHero.extraHurt = this._lastValue;
                break;
            case 22006:
                doAction(Const.BEGIN_MAIN_SKILL_5),
                    HeroController.getInstance().mainHero.tapCoin = this._lastValue,
                    doAction(Const.CHANGE_CLICK_COIN)
        }
        addAction(Const.LEVELUP_MAIN_SKILL, this.mainSkillLevelUp, this);
        addAction(Const.REBORN_SKILL_REFRESH, this.rebornSkillListChange, this)
    };
    c.prototype.mainSkillLevelUp = function(b) {
        if (b == this._skillModel.configId) {
            b = this._skillModel.config.base + this._skillModel.config.value * this._skillModel.lv;
            switch (this._skillModel.configId) {
                case 22002:
                    doAction(Const.STOP_MAIN_SKILL_VALUE_2);
                    doAction(Const.BEGIN_MAIN_SKILL_2, b);
                    break;
                case 22003:
                    HeroController.getInstance().mainHero.changeCritPro( - this._lastValue);
                    HeroController.getInstance().mainHero.changeCritPro(b);
                    break;
                case 22004:
                    doAction(Const.CHANGE_SPEED, 1 / (this._lastValue + 1));
                    doAction(Const.CHANGE_SPEED, b + 1);
                    break;
                case 22005:
                    this._lastValue += 1;
                    HeroController.getInstance().mainHero.extraHurt = b;
                    break;
                case 22006:
                    HeroController.getInstance().mainHero.tapCoin = b
            }
            this._lastValue = b
        }
    };
    c.prototype.rebornSkillListChange = function() {
        var b = this._skillModel.getLastTime();
        b != this._lastTimeBeforeChange && (this._lastTime += b - this._lastTimeBeforeChange, this._lastTimeBeforeChange = b)
    };
    c.prototype.countDown = function() {
        this._lastTime -= 1;
        if (0 >= this._lastTime) {
            Time.clearInterval(this._timeID);
            removeEventsByTarget(this);
            var b = this._skillModel.config.base + this._skillModel.config.value * this._skillModel.lv;
            switch (this._skillModel.configId) {
                case 22002:
                    doAction(Const.STOP_MAIN_SKILL_2);
                    break;
                case 22003:
                    doAction(Const.STOP_MAIN_SKILL_3);
                    HeroController.getInstance().mainHero.changeCritPro( - b);
                    break;
                case 22004:
                    doAction(Const.CHANGE_SPEED, 1 / (b + 1));
                    break;
                case 22005:
                    doAction(Const.STOP_MAIN_SKILL_4);
                    HeroController.getInstance().mainHero.extraHurt = 1;
                    break;
                case 22006:
                    doAction(Const.STOP_MAIN_SKILL_5),
                        HeroController.getInstance().mainHero.tapCoin = 0,
                        doAction(Const.CHANGE_CLICK_COIN)
            }
        }
    };
    return c
} ();
MainSkill.prototype.__class__ = "MainSkill";
var RebornSkillModel = function() {
    function c(b) {
        this.init(b)
    }
    c.prototype.init = function(b) {
        this.configId = b;
        this.config = RES.getRes("rebornConfig")[b];
        addFilter(Const.GET_HOLY_EFFECT, this.getHolyEffect, this)
    };
    c.prototype.getRes = function() {
        return "holy_icon_" + this.configId
    };
    c.prototype.lvLimit = function() {
        return this.config.maxLevel
    };
    c.prototype.getHolyEffect = function(b) {
        return b == getApplyString(this.config.effectType1) ? this.config.base1 + this.config.up1 * (this._lv - 1) : b
    };
    Object.defineProperty(c.prototype, "lv", {
        get: function() {
            return this._lv
        },
        set: function(b) {
            this._lv = b
        },
        enumerable: !0,
        configurable: !0
    });
    c.prototype.getCostNum = function() {
        return this.loop(this._lv)
    };
    c.prototype.loop = function(b) {
        return 1 == b ? Math.floor(this.config.cost + this.config.cost * Math.pow(1 + this.config.up2 / 2.5, 1)) : Math.floor(this.loop(b - 1) + this.config.cost * Math.pow(1 + this.config.up2 / 2.5, b))
    };
    c.prototype.getNeedGc = function() {
        var b = DataCenter.getInstance().totalData.rebornSkillList;
        return changeHolySc[b.length - 1] + this.bindCost
    };
    c.prototype.getNeedGc1 = function() {
        var b = DataCenter.getInstance().totalData.rebornSkillList;
        return changeHolySc[b.length] + this.bindCost
    };
    c.prototype.getTotalCost = function() {
        for (var b = this.callCost,
                 a = 1; a < this._lv; a++) b += this.loop(a);
        return b
    };
    c.prototype.refreshEffect = function() {
        SkillController.getInstance().addHolyEffect(this.config.effectType1, this.config.targetHero1, this.config.base1 + this.config.up1 * (this._lv - 1));
        SkillController.getInstance().addHolyAdd(this.config.base2 + this.config.up2 * (this._lv - 1))
    };
    c.prototype.getDes1 = function() {
        for (var b = [], a = 0; a < arguments.length; a++) b[a - 0] = arguments[a];
        b = b[0] || this._lv;
        0 == b && (b = 1);
        b = this.config.base1 + this.config.up1 * (b - 1);
        b *= 100;
        10 < b.toString().length && (b = b.toFixed(2));
        return this.config.des1.replace("{0}", b)
    };
    c.prototype.getDes2 = function() {
        for (var b = [], a = 0; a < arguments.length; a++) b[a - 0] = arguments[a];
        b = b[0] || this._lv;
        0 == b && (b = 1);
        b = this.config.base2 + this.config.up2 * (b - 1);
        b *= 100;
        10 < b.toString().length && (b = b.toFixed(2));
        return this.config.des2.replace("{0}", b)
    };
    c.prototype.destroy = function() {
        removeEventsByTarget(this);
        RebornSkillModelCache.getInstance().destroy(this.configId)
    };
    c.createByConfigId = function(b) {
        return RebornSkillModelCache.getInstance().getCache(b)
    };
    return c
} ();
RebornSkillModel.prototype.__class__ = "RebornSkillModel";
var RebornSkillModelCache = function() {
    function c() {
        this._models = []
    }
    c.getInstance = function() {
        null == this._instance && (this._instance = new c);
        return this._instance
    };
    c.prototype.getCache = function(b) {
        if (this._models[b]) return this._models[b];
        var a = new RebornSkillModel(b);
        return this._models[b] = a
    };
    c.prototype.destroy = function(b) {
        delete this._models[b]
    };
    return c
} ();
RebornSkillModelCache.prototype.__class__ = "RebornSkillModelCache";
var DataCenter = function() {
    function c() {
        this.currentTime = 0;
        this.paymentCountdown = this.countdown = this.achieve = this.statistics = null;
        this.achieveList = [];
        this.topList = [];
        this.topTime = this.top = 0;
        this.topPic = "";
        this._dieModel = this._paymentList = null
    }
    c.prototype.init = function() {
        this._currentDPSList = [];
        this._tempList = [];
        HeroController.getInstance().initHeroList();
        SkillController.getInstance().refreshSkillList();
        SkillController.getInstance().refreshHolySkillList();
        HeroController.getInstance().refreshHeroModel();
        MainTickController.getInstance().startTick()
    };
    c.getInstance = function() {
        null == this._instance && (this._instance = new c);
        return this._instance
    };
    Object.defineProperty(c.prototype, "paymentList", {
        get: function() {
            null == this._paymentList && (this._paymentList = PlatformFactory.getPlatform().getPayments());
            return this._paymentList
        },
        enumerable: !0,
        configurable: !0
    });
    c.prototype.initGameData = function(b) {
        this.totalData = new DataModel(b);
        this.currentTime = b.time || (new Date).getTime();
        this.countdown = new CountdownModel(b.countdown || {});
        var a = (new Date).getTime() - this.currentTime;
        1E3 < a && this.countdown.over(Math.floor(a / 1E3));
        this.paymentCountdown = new CountdownModel({});
        this.statistics = new StatisticsModel(b.statistics || {});
        this.statistics.notifyName = Const.CHANGE_STATISTICS;
        this.achieve = new StatisticsModel(b.achieve || {});
        this.achieve.notifyName = Const.CHANGE_STATISTICS;
        this.achieveList = [];
        b = RES.getRes("achieveConfig");
        for (var d in b) this.achieveList.push(new AchieveModel(b[d]));
        this.init()
    };
    c.prototype.getPaymentModel = function(b) {
        for (var a = 0; a < this.paymentList.length; a++) if (this.paymentList[a].goodType == b) return this.paymentList[a];
        return null
    };
    c.prototype.hasPaymentCountdown = function(b) {
        return 0 != this.countdown.getTime("payment_" + b)
    };
    c.prototype.getBossHP = function(b, a) {
        void 0 === b && (b = !1);
        void 0 === a && (a = !0);
        var d = 1,
            g = applyFilters(Const.GET_HOLY_EFFECT, getApplyString(24));
        g != getApplyString(24) && (d = 1 - g); ! 1 == a && (d = 1);
        var g = [2, 4, 6, 7, 10],
            e = c.getInstance().totalData,
            h = new NumberModel;
        if (GameUtils.isBoss() && b) {
            var f = (e.currentLevel - 1) % 5;
            h.add(29 * Math.pow(1.5704, e.currentLevel - 1) * g[f] * d)
        } else h.add(29 * Math.pow(1.5704, e.currentLevel - 1) * d);
        return h.getNumer()
    };
    c.prototype.getGcNum = function() {
        return this.totalData.gcNum.getNumer()
    };
    c.prototype.getScNum = function() {
        return this.totalData.scNum.getNumer()
    };
    c.prototype.hasEnoughSc = function(b) {
        return this.totalData.scNum.compare(b)
    };
    c.prototype.hasEnoughGc = function(b) {
        return this.totalData.gcNum.compare(b)
    };
    c.prototype.checkKillHero = function() {
        if (!this.hasPaymentCountdown("shield")) {
            var b = c.getInstance().totalData.currentLevel,
                a = 0.2,
                d = applyFilters(Const.GET_HOLY_EFFECT, getApplyString(27));
            d != getApplyString(27) && (a *= 1 - d);
            d = Math.random();
            50 < b && 0 == b % 5 && d < a && 0 < HeroController.getInstance().getAliveHero().length && c.getInstance().killHero()
        }
    };
    c.prototype.evolutionHero = function(b) {
        this.totalData.heroList[b.configId].lv += 1;
        c.getInstance().totalData.heroList[b.configId].skill = [];
        b.refreshHeroSkillList();
        b.refreshDataWithSkill();
        SkillController.getInstance().refreshSkillList();
        HeroController.getInstance().refreshHeroModel();
        doAction(Const.REFRESH_UNOPEN_LIST)
    };
    c.prototype.killHero = function() {
        var b = HeroController.getInstance().getAliveHero(),
            a = GameUtils.random(0, b.length - 1);
        b[a].killMe();
        SkillController.getInstance().refreshSkillList();
        HeroController.getInstance().refreshHeroModel();
        doAction(Const.HERO_DEAD, b[a]);
        doAction(Const.REFRESH_UNOPEN_LIST);
        this._dieModel = b[a]
    };
    c.prototype.addHero = function(b) {
        this.totalData.heroList[b.configId] = {};
        this.totalData.heroList[b.configId].lv = 1;
        this.totalData.heroList[b.configId].skill = [];
        this.totalData.heroList[b.configId].dieTime = 0;
        b.refreshDataWithSkill();
        HeroController.getInstance().addHero(b);
        SkillController.getInstance().refreshSkillList();
        HeroController.getInstance().refreshHeroModel();
        doAction(Const.ADD_HERO, b)
    };
    c.prototype.hasEnoughCallHero = function() {
        var b = c.getInstance().totalData.heroList,
            a = RES.getRes("heroConfig"),
            d;
        for (d in a) {
            var g = a[d];
            if ((!b.hasOwnProperty(d) || 0 == b[d].lv) && 0 == g.isMain && this.hasEnoughSc(GameUtils.getCallCost(g.callCost))) return ! 0
        }
        return ! 1
    };
    c.prototype.hasEnoughNewSkill = function() {
        var b = this.totalData.mainHero.skill,
            a = RES.getRes("skillConfig"),
            d;
        for (d in b) if (0 == b[d]) {
            var c = a[d];
            if (this.totalData.mainHero.lv >= c.level && this.hasEnoughSc(GameUtils.getCallCost(c.baseCost))) return ! 0
        }
        return ! 1
    };
    c.prototype.mainSkillUp = function(b, a) {
        void 0 === a && (a = 1);
        this.totalData.mainHero.skill[b] += a;
        if (1 == this.totalData.mainHero.skill[b]) {
            doAction(Const.UNLOCK_MAIN_SKILL);
            var d = RES.getRes("skillConfig");
            doAction(Const.SHOW_TIPS, "\u89e3\u9501\u65b0\u6280\u80fd:" + d[b].name)
        } else doAction(Const.LEVELUP_MAIN_SKILL, b)
    };
    c.prototype.levelUpHero = function(b, a) {
        void 0 === a && (a = 1);
        this.totalData.heroList[b.configId] ? (this.totalData.heroList[b.configId].lv += a, c.getInstance().statistics.addValue(StatisticsType.upgradehero, a.toString())) : b.isMainHero() && (this.totalData.mainHero.lv += a, doAction("mainHeroLevelUp"), this.statistics.updateValue(StatisticsType.maxLv, this.totalData.mainHero.lv));
        doAction(Const.HERO_UPGRADE, b);
        b.refreshDataWithSkill();
        HeroController.getInstance().refreshCurrentDPS()
    };
    c.prototype.unlockSkill = function(b, a) {
        this.totalData.heroList[b.configId].skill.push(a);
        b.refreshHeroSkillList();
        SkillController.getInstance().refreshSkillList();
        HeroController.getInstance().refreshHeroModel();
        doAction(Const.REFRESH_UNOPEN_LIST)
    };
    c.prototype.showDieLayer = function() {
        this._dieModel && (GameMainLayer.instance.showDieLayer(this._dieModel), this._dieModel = null)
    };
    c.prototype.bossLevelUp = function(b, a) {
        this.totalData.currentPass += 1;
        this.totalData.currentPass >= GameUtils.getMaxCustomPass() + 1 ? !1 == applyFilters(Const.ISBOSS, !1) ? doAction(Const.FIGHT_BOSS) : (this.totalData.currentPass = 1, this.totalData.currentLevel += 1, doAction(Const.BOSS_KILLED, b, a), doAction(Const.REFRESH_HOME_BG), this.showDieLayer(), 80 < this.totalData.currentLevel && 1 == this.totalData.currentLevel % 10 && this.dropHoly(), ProxyUtils.platformInfo != platformType_wanba && ProxyUtils.crossLevel(this.totalData.currentLevel)) : this.totalData.currentPass >= GameUtils.getMaxCustomPass() ? doAction(Const.FIGHT_BOSS) : doAction(Const.REFRESH_BOSS)
    };
    c.prototype.dropHoly = function() {
        doAction(Const.SHOW_HOLYCOIN)
    };
    c.prototype.reduceSc = function(b) {
        this.totalData.scNum.reduce(b);
        doAction(Const.CHANGE_COIN)
    };
    c.prototype.reduceGc = function(b, a) {
        void 0 === a && (a = !1);
        this.totalData.gcNum.reduce(b);
        doAction(Const.CHANGE_DIAMOND);
        if (!1 == a) {
            var d = new NumberModel;
            d.add(b);
            ProxyUtils.reduceDiamond(d.getData())
        }
    };
    c.prototype.changeGc = function(b) {
        this.totalData.gcNum.add(b);
        doAction(Const.CHANGE_DIAMOND)
    };
    c.prototype.refreshGc = function(b) {
        this.totalData.gcNum = new NumberModel;
        this.totalData.gcNum.add(b);
        doAction(Const.CHANGE_DIAMOND)
    };
    c.prototype.changeSc = function(b) {
        this.totalData.scNum.add(b);
        c.getInstance().statistics.addValue(StatisticsType.coin, b);
        doAction(Const.CHANGE_COIN)
    };
    c.prototype.changeHolyNum = function(b) {
        this.totalData.holyNum += b;
        doAction(Const.CHANGE_HOLY_NUM)
    };
    c.prototype.getHolyNum = function() {
        return this.totalData.holyNum
    };
    c.prototype.resetGame = function() {
        this.statistics.addValue(StatisticsType.loseSc, this.getScNum());
        this.statistics.addValue(StatisticsType.loseDPS, HeroController.getInstance().currentDPS);
        this.totalData.resetData();
        this.statistics.removeValue(StatisticsType.dps);
        this.statistics.removeValue(StatisticsType.level);
        this._currentDPSList = [];
        this._tempList = [];
        HeroController.getInstance().cleanHeroList();
        HeroController.getInstance().initHeroList();
        SkillController.getInstance().refreshSkillList();
        SkillController.getInstance().refreshHolySkillList();
        HeroController.getInstance().refreshHeroModel();
        doAction(Const.RESET_GAME);
        doAction(Const.CHANGE_COIN);
        var b = (new Date).getTime();
        this.statistics.resetData(StatisticsType.rebornTime, b)
    };
    c.prototype.destroyRebornSkill = function(b) {
        for (var a = 0; a < this.totalData.rebornSkillList.length && this.totalData.rebornSkillList[a].id != b.configId; a++);
        b.destroy();
        this.totalData.rebornSkillList.splice(a, 1);
        SkillController.getInstance().refreshHolySkillList();
        HeroController.getInstance().refreshHeroModel();
        doAction(Const.CHANGE_REBORN_SKILL);
        doAction(Const.REBORN_SKILL_REFRESH);
        a = b.getTotalCost();
        this.changeHolyNum(a);
        this.reduceGc(b.getNeedGc1())
    };
    c.prototype.unlockRebornSkill = function(b, a) {
        for (var d = [], c = 0; 29 > c; c++) d.push(10001 + c);
        for (c = 0; c < this.totalData.rebornSkillList.length; c++) {
            var e = d.indexOf(this.totalData.rebornSkillList[c].id); - 1 != e && d.splice(e, 1)
        }
        c = GameUtils.random(0, d.length - 1);
        e = {};
        e.id = d[c];
        e.lv = 1;
        e.cost = b;
        e.bind = a;
        this.totalData.rebornSkillList.push(e);
        SkillController.getInstance().refreshHolySkillList();
        HeroController.getInstance().refreshHeroModel();
        doAction(Const.CHANGE_REBORN_SKILL);
        doAction(Const.REBORN_SKILL_REFRESH);
        this.changeHolyNum( - b);
        d = RebornSkillModel.createByConfigId(e.id);
        GameMainLayer.instance.showHolyAnimation(d)
    };
    c.prototype.levelUpRebornSkill = function(b) {
        for (var a = 0; a < this.totalData.rebornSkillList.length; a++) if (this.totalData.rebornSkillList[a].id == b) {
            this.totalData.rebornSkillList[a].lv += 1;
            break
        }
        SkillController.getInstance().refreshHolySkillList();
        HeroController.getInstance().refreshHeroModel();
        doAction(Const.REFRESH_UNOPEN_LIST);
        doAction(Const.REBORN_SKILL_REFRESH)
    };
    c.prototype.useSkill = function(b) { (new MainSkill(b)).execute()
    };
    c.prototype.refreshSkillCD = function(b, a) {
        this.totalData.mainSkillCD[b] || (this.totalData.mainSkillCD[b] = {});
        this.totalData.mainSkillCD[b] = a
    };
    c.prototype.hasNewAchieve = function() {
        for (var b = 0; b < this.achieveList.length; b++) if (this.achieveList[b].canAward()) return ! 0;
        return ! 1
    };
    c.prototype.getAttackInfo = function() {
        var b = HeroController.getInstance().mainHero,
            a = b.critPro,
            d = !1;
        Math.random() < a && (d = !0);
        a = applyFilters(Const.ISBOSS, !1) ? b.bossDps: b.tapDamage;
        if (d) {
            this.statistics.addValue(StatisticsType.crit);
            var c = new NumberModel;
            c.add(a);
            c.times(b.critHurt);
            1 < b.extraHurt && c.times(b.extraHurt);
            a = c.getNumer()
        } else 1 < b.extraHurt && (c = new NumberModel, c.add(a), c.times(b.extraHurt), a = c.getNumer());
        return {
            crit: d,
            dmg: a
        }
    };
    c.prototype.getTheDamage = function(b) {
        var a = HeroController.getInstance().mainHero,
            d;
        d = applyFilters(Const.ISBOSS, !1) ? a.bossDps: a.tapDamage;
        var c = new NumberModel;
        c.add(d);
        c.times(b);
        1 < a.extraHurt && c.times(a.extraHurt);
        d = c.getNumer();
        return {
            dmg: d
        }
    };
    c.prototype.recordAttackDPS = function(b) {
        this._currentDPSList.push(b)
    };
    c.prototype.resetRecordAttack = function() {
        this._tempList = this._currentDPSList;
        this._currentDPSList = []
    };
    c.prototype.getRecordAttack = function() {
        return this._tempList
    };
    c.prototype.refreshCurrentDPS = function() {
        for (var b = this._tempList,
                 a = new NumberModel,
                 d = 0; d < b.length; d++) a.add(b[d]);
        doAction(Const.REFRESH_CURRENT_DPS, a.getNumer())
    };
    c.prototype.serialize = function() {
        var b = {};
        b.sc = encodeURIComponent(this.totalData.scNum.getData());
        b.gc = this.totalData.gcNum.getData();
        b.curLife = this.totalData.currentLife;
        b.curLv = this.totalData.currentLevel;
        b.curPass = this.totalData.currentPass;
        b.hl = this.totalData.heroList;
        b.time = this.currentTime;
        b.mh = this.totalData.mainHero;
        b.skillCD = this.totalData.mainSkillCD;
        b.statistics = this.statistics.getData();
        b.achieve = this.achieve.getData();
        b.showNum = this.totalData.showHeroNum;
        b.rebornSkill = this.totalData.rebornSkillList;
        b.holy = this.totalData.holyNum;
        b.countdown = this.countdown.getData();
        b.guideStep = this.totalData.guideStep;
        b.shareFalls = this.totalData.shareFalls;
        return b
    };
    c.prototype.saveData = function() {
        PlatformFactory.getPlatform().serialize(this)
    };
    return c
} ();
DataCenter.prototype.__class__ = "DataCenter";
var RenderTextureManager = function() {
    function c() {}
    c.getRenderTexture = function() {
        var b = c._pool.shift();
        b || (c.count++, egret.Logger.info("\u521b\u5efa\u7b2c" + c.count + "\u4e2aRenderTexture"), b = new egret.RenderTexture);
        return b
    };
    c.getBitmap = function(b) {
        var a = c.getRenderTexture();
        a.drawToTexture(b);
        b = new egret.Bitmap;
        b.texture = a;
        return b
    };
    c.destroyRenderTexture = function(b) {
        b.drawToTexture ? c._pool.push(b) : egret.Logger.warning("\u9500\u6bc1\u7684bitmap\u7eb9\u7406\u4e0d\u662fegret.RenderTexture")
    };
    c.destroyBitmap = function(b) {
        b.texture.drawToTexture ? c._pool.push(b.texture) : egret.Logger.warning("\u9500\u6bc1\u7684bitmap\u7eb9\u7406\u4e0d\u662fegret.RenderTexture")
    };
    c._pool = [];
    c.count = 0;
    return c
} ();
RenderTextureManager.prototype.__class__ = "RenderTextureManager";
var ResourceUtils = function() {
    function c() {}
    c.createBitmapByName = function(b) {
        var a = new egret.Bitmap;
        b = RES.getRes(b);
        a.texture = b;
        return a
    };
    c.createBitmapFromSheet = function(b, a) {
        void 0 === a && (a = "commonRes");
        var d = RES.getRes(a).getTexture(b),
            c = new egret.Bitmap;
        c.texture = d;
        return c
    };
    c.getTextureFromSheet = function(b, a) {
        void 0 === a && (a = "commonRes");
        return RES.getRes(a).getTexture(b)
    };
    c.removeFromParent = function(b) {
        b && b.parent && b.parent.removeChild(b)
    };
    return c
} ();
ResourceUtils.prototype.__class__ = "ResourceUtils";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    CoinTxt = function(c) {
        function b() {
            c.call(this);
            this.anchorX = this.anchorY = 0.5;
            var a = RES.getRes("coin_num_fnt_fnt");
            this._text = new egret.BitmapText;
            this.addChild(this._text);
            this._text.font = a;
            this._text.anchorX = this._text.anchorY = 0.5
        }
        __extends(b, c);
        b.prototype.start = function() {
            this.alpha = 1;
            egret.Tween.get(this).to({
                    y: 250,
                    alpha: 0.5
                },
                800).call(this.dispose, this)
        };
        b.prototype.start1 = function(a) {
            this.alpha = 1;
            egret.Tween.get(this).to({
                    y: a,
                    alpha: 0.5
                },
                500).call(this.dispose, this)
        };
        Object.defineProperty(b.prototype, "text", {
            get: function() {
                return this._text.text
            },
            set: function(a) {
                this._text.text = a
            },
            enumerable: !0,
            configurable: !0
        });
        b.prototype.dispose = function() {
            this.parent && (this.y = 300, GameUtils.recycleCoinTxt(this), this.parent.removeChild(this))
        };
        return b
    } (egret.DisplayObjectContainer);
CoinTxt.prototype.__class__ = "CoinTxt";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    HurtTxt = function(c) {
        function b() {
            c.call(this);
            this._runTime = 0;
            this.anchorX = this.anchorY = 0.5;
            var a = RES.getRes("blood_num_fnt_fnt");
            this._text = new egret.BitmapText;
            this._text.font = a;
            this.addChild(this._text);
            this.cacheAsBitmap = !0
        }
        __extends(b, c);
        b.prototype.start = function(a) {
            this.scaleX = a ? this.scaleY = 1.6 : this.scaleY = 1;
            this.alpha = 1;
            egret.Tween.get(this).to({
                    y: 150,
                    alpha: 0.3
                },
                800).call(this.dispose, this)
        };
        Object.defineProperty(b.prototype, "text", {
            get: function() {
                return this._text.text
            },
            set: function(a) {
                this._text.text = a
            },
            enumerable: !0,
            configurable: !0
        });
        b.prototype.loop = function(a) {
            this._runTime += a;
            40 < this._runTime && (this.alpha -= 0.025, this.y -= 2, 0 >= this.alpha && (egret.Ticker.getInstance().unregister(this.loop, this), this.dispose()))
        };
        b.prototype.dispose = function() {
            this.parent && (this.y = 300, GameUtils.recycleTxt(this), this.parent.removeChild(this))
        };
        return b
    } (egret.DisplayObjectContainer);
HurtTxt.prototype.__class__ = "HurtTxt";
var GameUtils = function() {
    function c() {}
    c.random = function(b, a) {
        null == a && (a = b, b = 0);
        return b + Math.floor(Math.random() * (a - b + 1))
    };
    c.getCallCost = function(b) {
        var a = applyFilters(Const.GET_HOLY_EFFECT, getApplyString(20));
        if (a == getApplyString(20)) return b;
        var d = new NumberModel;
        d.add(b);
        d.times(1 - a);
        return d.getNumer()
    };
    c.getMaxCustomPass = function() {
        var b = applyFilters(Const.GET_HOLY_EFFECT, getApplyString(26));
        return b != getApplyString(26) ? Math.ceil(10 * (1 - b)) : 10
    };
    c.getMapConfig = function(b) {
        void 0 === b && (b = null);
        var a = RES.getRes("map_config");
        null == b && (b = DataCenter.getInstance().totalData.currentLevel);
        b = Math.ceil(b % 100 / 5);
        0 == b && (b = 20);
        return a[b]
    };
    c.getMonsterID = function(b) {
        void 0 === b && (b = !1);
        var a = DataCenter.getInstance().totalData.currentLevel,
            d;
        if (b) {
            b = RES.getRes("map_config");
            var g = a %= 100;
            if (0 == a) return b[20].monster.split(",")[4];
            a = Math.ceil(a / 5);
            d = b[a].monster.split(",");
            return d[g - b[a].startLv]
        }
        b = RES.getRes("monster_config");
        a %= 100;
        0 == a && (a = 100);
        d = b[a].smallMonster.split(",");
        return d[c.random(1, d.length) - 1]
    };
    c.getMapRes = function(b) {
        void 0 === b && (b = null);
        null == b && (b = DataCenter.getInstance().totalData.currentLevel);
        var a;
        a = RES.getRes("map_config");
        var d = b %= 100;
        if (0 == b) return a[20].useMap.split(",")[4];
        b = Math.ceil(b / 5);
        return a[b].useMap.split(",")[d - a[b].startLv]
    };
    c.isBoss = function() {
        return DataCenter.getInstance().totalData.currentPass >= c.getMaxCustomPass()
    };
    c.replenish = function(b) {
        return 1 == b.length ? "0" + b: b
    };
    c.formatTime = function(b) {
        var a = Math.floor(Math.floor(b % 3600) / 60);
        b = Math.floor(Math.floor(b % 3600) % 60);
        b = "" + Math.floor(b / 10) + Math.floor(b % 10);
        return this.replenish("" + Math.floor(a / 10) + Math.floor(a % 10)) + ":" + this.replenish(b) + ""
    };
    c.formatTime1 = function(b) {
        var a = Math.floor(Math.floor(b % 3600) / 60).toString(),
            d = Math.floor(Math.floor(b % 3600) % 60).toString();
        return this.replenish(Math.floor(b / 3600).toString()) + ":" + this.replenish(a) + ":" + this.replenish(d)
    };
    c.formatTime3 = function(b) {
        var a = Math.floor(Math.floor(b % 3600) / 60).toString();
        return this.replenish(Math.floor(b / 3600).toString()) + "\u5c0f\u65f6" + this.replenish(a) + "\u5206\u949f"
    };
    c.formatTime2 = function(b) {
        return this.replenish(b.toString())
    };
    c.formatShortTime = function(b) {
        return 3600 <= b ? this.formatTime1(b) : 60 <= b ? this.formatTime(b) : this.formatTime2(b)
    };
    c.formatDay = function(b) {
        b = (new Date).getTime() - b;
        return Math.ceil(b / 864E5) + "\u5929"
    };
    c.removeAllChildren = function(b) {
        for (; b.numChildren;) b.removeChildAt(0)
    };
    c.getNewNumber = function(b) {
        return b.toString()
    };
    c.removeFromParent = function(b) {
        b && b.parent && b.parent.removeChild(b)
    };
    c.getWinWidth = function() {
        return egret.MainContext.instance.stage.stageWidth
    };
    c.getWinHeight = function() {
        return egret.MainContext.instance.stage.stageHeight
    };
    c.recycleCoinTxt = function(b) {
        c.coinTxtPool.push(b)
    };
    c.getCoinTxt = function(b) {
        var a;
        a = 0 == c.coinTxtPool.length ? new CoinTxt: c.coinTxtPool.pop();
        a.text = b;
        return a
    };
    c.recycleTxt = function(b) {
        c.hurtTxtPool.push(b)
    };
    c.getHurtTxt = function(b) {
        var a;
        a = 0 == c.hurtTxtPool.length ? new HurtTxt: c.hurtTxtPool.pop();
        a.text = b;
        return a
    };
    c.getQuery = function(b, a) {
        void 0 === a && (a = !1);
        var d = location.href; ! 0 == a && (d = d.replace("#", "&"));
        if ( - 1 < d.indexOf("?")) for (var d = d.split("?")[1].split("&"), c = 0; c < d.length; c++) {
            var e = d[c].split("=");
            if (e[0] == b) return e[1]
        }
        return null
    };
    c.createButton = function(b, a, d, c, e, h, f, k, l, m) {
        void 0 === m && (m = 16777215);
        var p = new egret.DisplayObjectContainer,
            n = new SimpleButton;
        n.width = h;
        n.height = f;
        n.x = c;
        n.y = e;
        n.useZoomOut(!0);
        n.addOnClick(d, this);
        n.anchorX = n.anchorY = 0.5;
        n.addChild(p);
        p.cacheAsBitmap = !0;
        a = ResourceUtils.createBitmapFromSheet(a);
        a.anchorX = a.anchorY = 0.5;
        p.addChild(a);
        a.x = h / 2;
        a.y = f / 2;
        a = new egret.TextField;
        a.anchorX = a.anchorY = 0.5;
        p.addChild(a);
        a.text = b;
        a.x = h / 2 + k;
        a.y = f / 2 + l;
        a.size = 25;
        a.textColor = m;
        return n
    };
    c.getQueryAnchor = function() {
        var b = location.href;
        return - 1 < b.indexOf("#") ? b.split("#")[1] : ""
    };
    c.preloadHeroMovieResource = function(b, a, d) {
        void 0 === d && (d = null);
        var g = "hero_" + b,
            e = [];
        this.parserAnimNeedLoadFiles("hero_" + b + "_json", e);
        c.preloadAnimations(e, g, a, d)
    };
    c.parserAnimNeedLoadFiles = function(b, a) {
        a.push(b);
        var d = b.split("_");
        d.splice(d.length - 1, 0, "swf");
        d = d.join("_");
        a.push(d)
    };
    c.preloadAnimation = function(b, a, d, c) {
        void 0 === a && (a = null);
        void 0 === d && (d = null);
        void 0 === c && (c = null);
        var e = [];
        this.parserAnimNeedLoadFiles(b, e);
        this.preloadAnimations(e, "group_" + b, a, d, c)
    };
    c.getPlatformType = function() {
        var b = this.getQuery("pf");
        return null == b ? ProxyUtils.platformInfo: globalData.openid && "" != globalData.openid ? platformType_wanba: b
    };
    c.empty = function(b) {
        return "" == b || void 0 == b || null == b
    };
    c.notEmpty = function(b) {
        return ! this.empty(b)
    };
    c.distance = function(b, a, d, c) {
        return Math.sqrt((b - d) * (b - d) + (a - c) * (a - c))
    };
    c.preloadAnimations = function(b, a, d, c, e) {
        void 0 === d && (d = null);
        void 0 === c && (c = null);
        void 0 === e && (e = null);
        var h = this;
        if (RES.isGroupLoaded(a)) d && d.call(c, e);
        else {
            var f = function(b) {
                b.groupName == a && (RES.removeEventListener(RES.ResourceEvent.GROUP_COMPLETE, f, h), d && d.call(c, e))
            };
            RES.createGroup(a, b, !0);
            RES.addEventListener(RES.ResourceEvent.GROUP_COMPLETE, f, h);
            RES.loadGroup(a)
        }
    };
    c.cacheRender = function(b, a, d) {
        b && b.parent && b.parent.removeChild(b);
        d && RenderTextureManager.destroyBitmap(d);
        d = RenderTextureManager.getBitmap(b);
        a.addChild(d);
        return d
    };
    c.hurtTxtPool = [];
    c.coinTxtPool = [];
    return c
} ();
GameUtils.prototype.__class__ = "GameUtils";
var PlatformTypes = function() {
    function c() {}
    c.WEIXIN = "wx";
    c.EGRET = "egret";
    c.WANBA = "wb";
    return c
} ();
PlatformTypes.prototype.__class__ = "PlatformTypes";
var Guide = function() {
    function c() {}
    c.doGuide = function(b) {};
    c.updateStatus = function(b) {};
    c.HIDE_BOTTOM = "hide_bottom";
    c.SHOW_BOTTOM1 = "show_bottom_1";
    c.SHOW_BOTTOM2 = "show_bottom_2";
    c.CLICK_HOME = "click_home";
    c.UP_HERO = "up_hero";
    c.UNLOCK_MAIN_SKILL = "unlock_main_skill";
    c.CLICK_DOWN_JIANTOU = "click_down_jiantou";
    c.BUY_HOLY = "buy_holy";
    c.USE_SKILL = "use_skill";
    c.BUY_HERO = "buy_hero";
    c.IS_SKILL_LAYER_EXSIT = "is_skill_layer_exsit";
    c.IS_SKILL_CDING = "is_skill_cding";
    c.handPositions = {
        hitBoss: {
            x: 325,
            y: 425,
            pow: 0
        },
        upSkill: {
            x: 535,
            y: 655,
            pow: 1
        },
        buyHero: {
            x: 535,
            y: 655,
            pow: 2
        },
        clickStore: {
            x: 565,
            y: 935,
            pow: 3
        },
        showStoreSkill: {
            x: 535,
            y: 865,
            pow: 4
        },
        bottom2: {
            x: 230,
            y: 925
        },
        useSkill: {
            x: 70,
            y: 830
        },
        bottom3: {
            x: 410,
            y: 925
        },
        clickMain: {
            x: 85,
            y: 925,
            pow: 5
        },
        buyMainSkill: {
            x: 525,
            y: 755,
            pow: 6
        },
        clickSanjiao: {
            x: 595,
            y: 570,
            pow: 7
        },
        showMainSkill: {
            x: 70,
            y: 830,
            pow: 8
        },
        buyShengwu: {
            x: 535,
            y: 655,
            pow: 10
        }
    };
    return c
} ();
Guide.prototype.__class__ = "Guide";
var TouchController = function() {
    function c(b, a, d) {
        void 0 === d && (d = 0.1);
        this._scaleObj = {};
        this._scaleFactor = d;
        this._selector = b;
        this._context = a
    }
    c.prototype.dispose = function() {
        this._context = this._selector = null
    };
    c.prototype.message = function(b, a, d, c) {
        var e = this;
        this._scaleObj.hasOwnProperty(a.hashCode) || (this._scaleObj[a.hashCode] = {
            scaleX: a.scaleX,
            scaleY: a.scaleY
        });
        if (0 == b) {
            if (0 < this._scaleFactor) {
                var h = this._scaleObj[a.hashCode];
                egret.Tween.removeTweens(a);
                egret.Tween.get(a).to({
                        scaleX: h.scaleX - this._scaleFactor,
                        scaleY: h.scaleY - this._scaleFactor
                    },
                    100);
                return
            }
        } else if (1 == b && (h = a, 0 < this._scaleFactor)) {
            h = this._scaleObj[a.hashCode];
            egret.Tween.removeTweens(a);
            egret.Tween.get(a).to({
                    scaleX: h.scaleX,
                    scaleY: h.scaleY
                },
                100).call(function(a) {
                    delete e._scaleObj[a.hashCode]
                },
                this, [a]);
            return
        }
        this.doCallback(b, a, d, c)
    };
    c.prototype.doCallback = function(b, a, d, c) {
        this._selector && this._context && this._selector.call(this._context, b, a, d, c)
    };
    return c
} ();
TouchController.prototype.__class__ = "TouchController";
var AnimationClear = function() {
    function c() {}
    c.clear = function(b) {
        b && (egret.Tween.removeTweens(b), c.clearAnimation(b))
    };
    c.clearAnimation = function(b) {
        b = b.hashCode;
        if (c._animationMap.hasOwnProperty(b)) {
            for (var a = c._animationMap[b]; 0 < a.length;) a.shift().dispose();
            delete c._animationMap[b]
        }
    };
    c.addAnimation = function(b) {
        var a = b.getTarget().hashCode;
        c._animationMap.hasOwnProperty(a) || (c._animationMap[a] = []);
        c._animationMap[a].push(b)
    };
    c.removeAnimation = function(b) {
        var a = b.getTarget().hashCode;
        if (c._animationMap.hasOwnProperty(a)) {
            var d = c._animationMap[a],
                g = d.indexOf(b); - 1 < g && d.splice(g, 1);
            b.dispose();
            0 == d.length && delete c._animationMap[a]
        }
    };
    c._animationMap = {};
    return c
} ();
AnimationClear.prototype.__class__ = "AnimationClear";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    DropAnimation = function(c) {
        function b(a, b) {
            c.call(this, a);
            this._speedY = -450;
            this._a = 750;
            this._speedX = b
        }
        __extends(b, c);
        b.prototype.update = function(a) {
            1 < a && (a = 1);
            this._target.x = this.startX + this._speedX * a;
            this._target.y = this.startY + (this._speedY * a + this._a * a * a)
        };
        b.prototype.run = function(a) {
            this.startX = a.x;
            this.startY = a.y;
            c.prototype._run.call(this, a)
        };
        return b
    } (GameAnimation);
DropAnimation.prototype.__class__ = "DropAnimation";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    DropAnimation1 = function(c) {
        function b(a, b) {
            c.call(this, a);
            this._speedY = -50;
            this._a = 50;
            this._speedX = b
        }
        __extends(b, c);
        b.prototype.update = function(a) {
            1 < a && (a = 1);
            this._target.x = this.startX + this._speedX * a;
            this._target.y = this.startY + (this._speedY * a + this._a * a * a)
        };
        b.prototype.run = function(a) {
            this.startX = a.x;
            this.startY = a.y;
            c.prototype._run.call(this, a)
        };
        return b
    } (GameAnimation);
DropAnimation1.prototype.__class__ = "DropAnimation1";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    DropAnimation2 = function(c) {
        function b(a, b) {
            c.call(this, a);
            this._speedY = -400;
            this._a = 412;
            this._speedX = b
        }
        __extends(b, c);
        b.prototype.update = function(a) {
            1 < a && (a = 1);
            this._target.x = this.startX + this._speedX * a;
            this._target.y = this.startY + (this._speedY * a + this._a * a * a)
        };
        b.prototype.run = function(a) {
            this.startX = a.x;
            this.startY = a.y;
            c.prototype._run.call(this, a)
        };
        return b
    } (GameAnimation);
DropAnimation2.prototype.__class__ = "DropAnimation2";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    MovieBone = function() {
        function c(b, a, d, c, e, h) {
            void 0 === h && (h = !1);
            this.parentBoneName = b;
            this.display = a;
            this.boneName = d;
            this.completeRemove = c;
            this.zorder = e;
            this.isAdded = h
        }
        c.prototype.addToParent = function(b) {
            b = b.getChildByName(this.parentBoneName);
            null == this.zorder ? b.addChild(this.display) : b.addChildAt(this.display, this.zorder);
            this.isAdded = !0
        };
        c.prototype.clear = function() {
            this.display && this.display.parent && GameUtils.removeAllChildren(this.display.parent)
        };
        c.prototype.removeFromParent = function() {
            GameUtils.removeFromParent(this.display)
        };
        return c
    } ();
MovieBone.prototype.__class__ = "MovieBone";
var MovieInfo = function() {
    function c(b, a) {
        this.filename = b;
        this.armature = a;
        var d = this.parserTemplateFile(b),
            c = RES.getRes(d[1]),
            e = RES.getRes(d[0]),
            h = new starlingswf.SwfAssetManager;
        h.addSpriteSheet(d[0], e);
        this.swfObj = new starlingswf.Swf(c, h, 24)
    }
    c.prototype.parserTemplateFile = function(b) {
        var a = [b];
        b = b.split("_");
        b.splice(b.length - 1, 0, "swf");
        a.push(b.join("_"));
        return a
    };
    return c
} ();
MovieInfo.prototype.__class__ = "MovieInfo";
var MovieCache = function() {
    function c() {
        this._cache = {};
        this._info = {}
    }
    c.prototype.getInfo = function(b, a) {
        var d = [b, a].join("-");
        this._info[d] || (this._info[d] = new MovieInfo(b, a));
        return this._info[d]
    };
    c.prototype.getMovie = function(b, a, d) {
        var c = [b, a, d].join("-");
        this._cache[c] || (this._cache[c] = []);
        if (0 == this._cache[c].length) {
            var e = this.getInfo(b, a).swfObj.createMovie("mc_" + a + "_" + d);
            e.filename = b;
            e.armature = a;
            e.action = d;
            this._cache[c].push(e)
        }
        return this._cache[c].pop()
    };
    c.prototype.setMovie = function(b) {
        var a = [b.filename, b.armature, b.action].join("-");
        this._cache[a] || (this._cache[a] = []);
        this._cache[a].push(b)
    };
    c.getInstance = function() {
        null == this._inst && (this._inst = new c);
        return this._inst
    };
    return c
} ();
MovieCache.prototype.__class__ = "MovieCache";
var Movie = function(c) {
    function b(a, b, g) {
        c.call(this);
        this._bones = [];
        this._boneCount = 0;
        this._autoRemove = !0;
        this._isCache = this._isComplete = this._autoDispose = this._forceCompleteRemove = !1;
        this._currentActionName = null;
        this._isPause = !1;
        this._allowSound = !0;
        this._filename = a;
        this._armatureName = b
    }
    __extends(b, c);
    Object.defineProperty(b.prototype, "autoDispose", {
        get: function() {
            return this._autoDispose
        },
        set: function(a) { (this._autoDispose = a) && this._isComplete && this.dispose()
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(b.prototype, "allowSound", {
        get: function() {
            return this._allowSound
        },
        set: function(a) {
            this._allowSound = a
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(b.prototype, "isCache", {
        get: function() {
            return this._isCache
        },
        set: function(a) {
            this._isCache = a
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(b.prototype, "autoRemove", {
        get: function() {
            return this._autoRemove
        },
        set: function(a) {
            this._autoRemove = a
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(b.prototype, "forceCompleteRemove", {
        get: function() {
            return this._forceCompleteRemove
        },
        set: function(a) {
            this._forceCompleteRemove = a
        },
        enumerable: !0,
        configurable: !0
    });
    b.prototype.play = function(a, b) {
        void 0 === b && (b = !1);
        this._currentActionName = a;
        this.removeListeners();
        this._isPause = !1;
        this._movieClip = MovieCache.getInstance().getMovie(this._filename, this._armatureName, a);
        this._movieClip.gotoAndPlay(1);
        this._movieClip.addEventListener(egret.Event.COMPLETE, this.onComplete, this);
        this._movieClip.addEventListener(starlingswf.SwfEvent.SWF_FRAME, this.onFrame, this);
        this._movieClip.rewind = b;
        this.addChild(this._movieClip);
        this._movieClip.loop && (this._autoRemove = !1)
    };
    b.prototype.setCallback = function(a, b) {
        void 0 === b && (b = null);
        for (var c = [], e = 2; e < arguments.length; e++) c[e - 2] = arguments[e];
        this._selector = a;
        this._context = b;
        this._args = c
    };
    b.prototype.onComplete = function(a) {
        this.doCallback(b.PLAY_COMPLETE);
        this.removeBones();
        if (this._autoRemove || this._forceCompleteRemove) this.doCallback(b.REMOVED),
            GameUtils.removeFromParent(this),
            this._isComplete = !0
    };
    b.prototype.removeListeners = function() {
        this._movieClip && (this.pause(!0), this.removeMovie())
    };
    b.prototype.doCallback = function(a, b) {
        void 0 === b && (b = null);
        this._selector && this._selector.apply(this._context, this._args.concat(a, b, this))
    };
    b.prototype.onFrame = function(a) {
        for (var b = 0; b < a.frames.length; b++) this.onFrameItem(a.frames[b])
    };
    b.prototype.onFrameItem = function(a) {
        a = a.split("-");
        var b = "",
            c = "";
        2 <= a.length ? (b = a[0], c = a[1]) : c = a[0];
        this.doCallback(c, b);
        2 <= a.length && "sound" == a[0] && this.playSound(a[1].replace(/\./gi, "_"));
        2 <= a.length && "bone" == a[0] && this.refreshBones(a[1])
    };
    b.prototype.refreshBones = function(a) {
        var b = this;
        this._bones.filter(function(b) {
            return b.parentBoneName == a && !b.isAdded
        }).forEach(function(a) {
            a.addToParent(b._movieClip)
        })
    };
    b.prototype.removeBones = function() {
        for (var a = this._bones.filter(function(a) {
            return a.completeRemove
        }); a.length;) this.removeBoneDisplay(a.shift().boneName)
    };
    b.prototype.playSound = function(a) {};
    b.prototype.addBoneDisplay = function(a, b, c, e, h) {
        void 0 === c && (c = null);
        void 0 === e && (e = !1);
        void 0 === h && (h = "");
        this._boneCount++;
        var f = "custom_bone_" + this._boneCount;
        "" != h && (f = h);
        a = new MovieBone(a, b, f, e, c);
        this._bones.push(a);
        return f
    };
    b.prototype.removeBoneDisplay = function(a) {
        for (var b = 0; b < this._bones.length && this._bones[b].boneName != a; b++);
        this._bones[b].removeFromParent();
        this._bones.splice(b, 1)
    };
    b.prototype.resume = function(a) {
        void 0 === a && (a = !1);
        this._movieClip && this._isPause && (this._movieClip.play(), this._isPause = !1, a && (this._movieClip.addEventListener(egret.Event.COMPLETE, this.onComplete, this), this._movieClip.addEventListener(starlingswf.SwfEvent.SWF_FRAME, this.onFrame, this)))
    };
    b.prototype.pause = function(a) {
        void 0 === a && (a = !1);
        this._movieClip && !this._isPause && (this._movieClip.stop(!0), this._isPause = !0, a && (this._movieClip.removeEventListener(egret.Event.COMPLETE, this.onComplete, this), this._movieClip.removeEventListener(starlingswf.SwfEvent.SWF_FRAME, this.onFrame, this)))
    };
    b.prototype.gotoAndPlay = function(a) {
        this._movieClip.setCurrentFrame(Number(a))
    };
    b.prototype.gotoAndStop = function(a) {
        this._movieClip.gotoAndStop(a)
    };
    b.prototype._onRemoveFromStage = function() {
        c.prototype._onRemoveFromStage.call(this);
        this.pause(!0)
    };
    b.prototype._onAddToStage = function() {
        c.prototype._onAddToStage.call(this);
        this.resume(!0)
    };
    b.prototype.removeMovie = function() {
        this._movieClip && (MovieCache.getInstance().setMovie(this._movieClip), GameUtils.removeFromParent(this._movieClip), this._movieClip = null)
    };
    b.prototype.dispose = function() {
        this.pause(!0);
        for (this.removeMovie(); 0 < this._bones.length;) this._bones.shift().clear();
        this._context = this._selector = this._args = null;
        c.prototype.dispose.call(this)
    };
    b.PLAY_COMPLETE = "complete";
    b.REMOVED = "removed";
    return b
} (BaseContainer);
Movie.prototype.__class__ = "Movie";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    LevelUpButton = function(c) {
        function b(a) {
            c.call(this);
            this._status = 0;
            this._showTime = 2E3;
            this._lastTime = this._timeID = -1;
            this._tickTime = 500;
            this._type = "";
            this._lockEffect = this._isUnlockSkill = !1;
            this.initButton(a)
        }
        __extends(b, c);
        b.prototype.initButton = function(a) {
            this._type = a;
            a = [];
            a = "lvup100_btn hero_lvup10_disable lvup10_btn hero_lvup10_disable hero_lvup_btn hero_lvup_disable".split(" ");
            this.button100 = new LevelUpButton1;
            this.button100.init(100, a[0], a[1]);
            this.button100.addOnClick(this.onClickHandler100, this);
            this.button100.useZoomOut(!0);
            this.addChild(this.button100);
            this.button100.x = -245;
            this.button10 = new LevelUpButton1;
            this.button10.init(10, a[2], a[3]);
            this.button10.addOnClick(this.onClickHandler10, this);
            this.button10.useZoomOut(!0);
            this.addChild(this.button10);
            this.button10.x = -130;
            this.button100.y = this.button10.y = 8;
            this.button1 = new LevelUpButton2;
            this.button1.init(a[4], a[5]);
            this.button1.addOnClick(this.onClickHandler1, this);
            this.button1.useZoomOut(!0);
            this.addChild(this.button1);
            this.button1.y = 8;
            this.button1.width = 165;
            this.button1.height = 68;
            this.button10.width = 115;
            this.button10.height = 68;
            this.button100.width = 115;
            this.button100.height = 68;
            this.resetStatus(0)
        };
        b.prototype.refreshData = function(a, b, c) {
            void 0 === b && (b = !1);
            void 0 === c && (c = 0);
            this._data = a;
            this._isSpecial = b;
            this._idx = c;
            this.refreshAllButton()
        };
        b.prototype.refreshAllButton = function() {
            this.refreshButton()
        };
        b.prototype.onClickHandler100 = function() {
            this.doCallBack(100, this._costList[2])
        };
        b.prototype.onClickHandler10 = function() {
            this.doCallBack(10, this._costList[1]);
            this.button1.removewNewTip()
        };
        b.prototype.onClickHandler1 = function() { ! 1 == this._isUnlockSkill ? this.doCallBack(1, this._costList[0]) : this.doCallBack(1E3, this._costList[0]);
            this.button1.removewNewTip()
        };
        b.prototype.setCallBack = function(a, b) {
            this._context = a;
            this._selector = b
        };
        b.prototype.doCallBack = function(a, b) {
            if (!1 == this._lockEffect) if (0 == this._status) {
                var c = (new Date).getTime(); - 1 == this._lastTime ? this._lastTime = c: c - this._lastTime < this._tickTime ? (this.resetStatus(1), -1 != this._timeID && Time.clearTimeout(this._timeID), this._timeID = Time.setTimeout(this.timeOut, this, this._showTime)) : this._lastTime = c
            } else - 1 != this._timeID && Time.clearTimeout(this._timeID),
                this._timeID = Time.setTimeout(this.timeOut, this, this._showTime);
            this.hideButton();
            this._context && this._selector && this._selector.call(this._context, a, b)
        };
        b.prototype.timeOut = function() {
            this.resetStatus(0)
        };
        b.prototype.refreshButton = function() {
            this._lockEffect = this._isUnlockSkill = !1;
            this._costList = [];
            this.button1.hideYellow();
            if (this._isSpecial) if (0 == this._idx) {
                var a = HeroController.getInstance().mainHero,
                    b = a.getCostNum();
                this._costList = b;
                var c = DataCenter.getInstance().hasEnoughSc(b[0]);
                this.button1.setEnabled(c);
                c = DataCenter.getInstance().hasEnoughSc(b[1]);
                this.button10.setEnabled(c);
                c = DataCenter.getInstance().hasEnoughSc(b[2]);
                this.button100.setEnabled(c);
                this.button10.refreshView(b[1]);
                this.button100.refreshView(b[2]);
                this.button1.refreshView(b[0], "+" + a.nextTapDamage + " \u70b9\u51fb\u4f24\u5bb3", "\u5347\u7ea7")
            } else 7 == this._idx && (a = HeroController.getInstance().mainHero, 600 <= a.lv ? (this.button1.setEnabled(!0), this.button1.refreshView("", "", "\u91cd\u751f")) : (this.button1.setEnabled(!1), this.button1.refreshView("", "", "Lv.600\u89e3\u9501")));
            else if (this._data instanceof HeroModel) {
                if (this._data.isDead()) {
                    this.button1.setEnabled(!1);
                    return
                }
                this._costList = a = this._data.getCostNum();
                c = DataCenter.getInstance().hasEnoughSc(a[1]);
                this.button10.setEnabled(c && this._data.canLevelUp(10));
                c = DataCenter.getInstance().hasEnoughSc(a[2]);
                this.button100.setEnabled(c && this._data.canLevelUp(100));
                this.button10.refreshView(a[1]);
                this.button100.refreshView(a[2]);
                var e = "\u5347\u7ea7";
                0 == this._data.lv ? e = "\u96c7\u4f63": 1E3 == this._data.lv && (e = "\u8fdb\u5316"); (b = this._data.nextSkill) && b.getOpenLevel() <= this._data.getSkillOpenLv() ? (this.button1.showYellow(), a = new NumberModel, c = new NumberModel, this._data.isEvolution() ? a.add(GameUtils.getCallCost(b.config.rebornCost)) : a.add(GameUtils.getCallCost(b.config.cost)), c.add(this._data.config.callCost), a.times(c.getDataNum()), a = a.getNumer(), c = DataCenter.getInstance().hasEnoughSc(a), this.button1.setEnabled(c), this.button1.refreshView(a, "" + b.config.name, "\u89e3\u9501\u6280\u80fd"), this._costList[0] = a, this._isUnlockSkill = !0) : (1E3 == this._data.lv && (c = new NumberModel, c.add(this._data.config.rebornRatio), b = new NumberModel, b.add(this._data.config.callCost), c.times(b.getDataNum()), a[0] = c.getNumer()), c = DataCenter.getInstance().hasEnoughSc(a[0]), this.button1.setEnabled(c), c && 0 == this._data.lv ? this.button1.createNewTip() : this.button1.removewNewTip(), 1E3 == this._data.lv ? this.button1.refreshView(a[0], "", e) : this.button1.refreshView(a[0], "+" + this._data.nextDPS + " DPS", e))
            } else this._data instanceof SkillModel && (this._lockEffect = !0, this.timeOut(), a = this._data.config.level, c = HeroController.getInstance().mainHero.lv, a > c ? (b = this._data.getMainCost(), this.button1.setEnabled(!1), this.button1.refreshView(b, "", "Lv." + a + "\u89e3\u9501")) : (b = this._data.getMainCost(), c = DataCenter.getInstance().hasEnoughSc(b), this.button1.setEnabled(c), 0 == this._data.lv ? (this.button1.refreshView(b, "", "\u70b9\u4eae"), c ? this.button1.createNewTip() : this.button1.removewNewTip()) : this.button1.refreshView(b, "+" + this._data.config.value * this._data.config.showTimes, "\u5347\u7ea7"), this._costList.push(b)));
            this.hideButton()
        };
        b.prototype.createNewTip = function() {
            this.button1.createNewTip()
        };
        b.prototype.removeNewTip = function() {
            this.button1.removewNewTip()
        };
        b.prototype.hideButton = function() { ! 1 == this.button10._enabled && (this.button10.visible = !1); ! 1 == this.button100._enabled && (this.button100.visible = !1)
        };
        b.prototype.resetStatus = function(a) {
            this._status = a;
            1 == this._status ? (this.button10.visible = !0, this.button100.visible = !0) : (this.button10.visible = !1, this.button100.visible = !1)
        };
        b.prototype.dispose = function() {
            c.prototype.dispose.call(this); - 1 != this._timeID && Time.clearTimeout(this._timeID)
        };
        return b
    } (BaseContainer);
LevelUpButton.prototype.__class__ = "LevelUpButton";
var LevelUpButton1 = function(c) {
    function b() {
        c.call(this);
        this.anchorX = this.anchorY = 0.5
    }
    __extends(b, c);
    b.prototype.init = function(a, b, c) {
        b = ResourceUtils.createBitmapFromSheet(b, "commonRes");
        this.addChild(b);
        c = ResourceUtils.createBitmapFromSheet(c, "commonRes");
        c.visible = !1;
        this.addChild(c);
        c = ResourceUtils.createBitmapFromSheet("coin_icon", "commonRes");
        this.addChild(c);
        c.x = 30;
        c.y = 10;
        c.scaleX = c.scaleY = 0.5;
        c = new egret.TextField;
        this.addChild(c);
        c.text = "+" + a;
        c.width = 60;
        c.height = 30;
        c.size = 20;
        c.x = 45;
        c.y = 30;
        this._costLabel = new egret.TextField;
        this.addChild(this._costLabel);
        this._costLabel.size = 18;
        this._costLabel.width = 80;
        this._costLabel.x = 50;
        this._costLabel.y = 10
    };
    b.prototype.refreshView = function(a) {
        this._costLabel.text = a
    };
    return b
} (SimpleButton);
LevelUpButton1.prototype.__class__ = "LevelUpButton1";
var LevelUpButton2 = function(c) {
    function b() {
        c.call(this);
        this._newHeroTip = null;
        this.anchorX = this.anchorY = 0.5
    }
    __extends(b, c);
    b.prototype.setEnabledState = function() {
        this._yellowContainer.visible ? (this._yellowContainer.getChildAt(0).visible = this._enabled, this._yellowContainer.getChildAt(1).visible = !this._enabled) : (this._normal.visible = this._enabled, this._dis && (this._dis.visible = !this._enabled))
    };
    b.prototype.showYellow = function() {
        this._yellowContainer.visible = !0
    };
    b.prototype.hideYellow = function() {
        this._yellowContainer.visible = !1
    };
    b.prototype.init = function(a, b, c) {
        void 0 === c && (c = "coin_icon");
        this._container = new egret.DisplayObjectContainer;
        this._normal = ResourceUtils.createBitmapFromSheet(a, "commonRes");
        this._container.addChild(this._normal);
        this._dis = ResourceUtils.createBitmapFromSheet(b, "commonRes");
        this._dis.visible = !1;
        this._container.addChild(this._dis);
        this._yellowContainer = new egret.DisplayObjectContainer;
        this._container.addChild(this._yellowContainer);
        a = ResourceUtils.createBitmapFromSheet("unlock_skill_btn");
        this._yellowContainer.addChild(a);
        b = ResourceUtils.createBitmapFromSheet(b);
        this._yellowContainer.addChild(b);
        this._yellowContainer.visible = !1;
        this._coinIcon = ResourceUtils.createBitmapFromSheet(c);
        this._container.addChild(this._coinIcon);
        "coin_icon" == c ? (this._coinIcon.x = 42, this._coinIcon.y = -20, this._coinIcon.scaleX = this._coinIcon.scaleY = 0.54) : (this._coinIcon.x = 42, this._coinIcon.y = -20, this._coinIcon.scaleX = this._coinIcon.scaleY = 0.67);
        this._staticLabel = new egret.TextField;
        this._container.addChild(this._staticLabel);
        this._staticLabel.text = "\u5347\u7ea7";
        this._staticLabel.width = 150;
        this._staticLabel.x = 85;
        this._staticLabel.y = 12;
        this._staticLabel.size = 20;
        this._staticLabel.textColor = 0;
        this._staticLabel.textAlign = egret.HorizontalAlign.CENTER;
        this._staticLabel.anchorX = 0.5;
        this._costLabel = new egret.TextField;
        this._container.addChild(this._costLabel);
        this._costLabel.x = 70;
        this._costLabel.y = -20;
        this._costLabel.size = 20;
        this._costLabel.width = 100;
        this._costLabel.textColor = 16777215;
        this._valueLabel = new egret.TextField;
        this._container.addChild(this._valueLabel);
        this._valueLabel.x = 80;
        this._valueLabel.y = 35;
        this._valueLabel.size = 18;
        this._valueLabel.textAlign = egret.HorizontalAlign.CENTER;
        this._valueLabel.anchorX = 0.5;
        this._valueLabel.textColor = 0;
        addFilter(Const.GET_NEW_HERO, this.getNewHero, this);
        this.addChild(this._container);
        this._container.cacheAsBitmap = !0
    };
    b.prototype.getNewHero = function(a) {
        this._newHeroTip && a++;
        return a
    };
    b.prototype.createNewTip = function() {
        this._newHeroTip || (this._newHeroTip = ResourceUtils.createBitmapFromSheet("new_hero_tip", "commonRes"), this._newHeroTip.x = 20, this._newHeroTip.y = 20, this._newHeroTip.anchorX = this._newHeroTip.anchorY = 0.5, this.addChild(this._newHeroTip), egret.Tween.get(this._newHeroTip, {
            loop: !0
        }).to({
                scaleX: 1.2,
                scaleY: 1.2
            },
            500).to({
                scaleX: 1,
                scaleY: 1
            },
            500))
    };
    b.prototype.removewNewTip = function() {
        this._newHeroTip && (egret.Tween.removeTweens(this._newHeroTip), this.removeChild(this._newHeroTip), this._newHeroTip = null)
    };
    b.prototype.refreshView = function(a, b, c) {
        this._costLabel.text = a;
        this._valueLabel.text = b;
        this._staticLabel.text = c;
        this._coinIcon.visible = "" == a ? !1 : !0;
        "" == b ? this._staticLabel.y = 22 : (this._staticLabel.y = 12, this._valueLabel.y = 35)
    };
    return b
} (SimpleButton);
LevelUpButton2.prototype.__class__ = "LevelUpButton2";
var BoxSkill = function() {
    function c(b) {
        this.config = RES.getRes("skillConfig")[b]
    }
    c.prototype.execute = function() {
        var b = DataCenter.getInstance().totalData.mainHero.skill;
        this._lv = 1;
        0 < b[this.config.configId] && (this._lv = b[this.config.configId]);
        doAction(Const.LOCK_SKILL, this.config.configId);
        b = this.config.base + this.config.value * this._lv;
        Time.setTimeout(this.countDown, this, 15E3);
        switch (this.config.configId) {
            case 22002:
                doAction(Const.BEGIN_MAIN_SKILL_2, b);
                break;
            case 22003:
                doAction(Const.BEGIN_MAIN_SKILL_3);
                HeroController.getInstance().mainHero.changeCritPro(b);
                break;
            case 22004:
                doAction(Const.CHANGE_SPEED, b + 1);
                break;
            case 22005:
                doAction(Const.BEGIN_MAIN_SKILL_4),
                    HeroController.getInstance().mainHero.extraHurt = b + 1
        }
        addFilter(Const.IS_BOX_SKILL_USING, this.isBoxSkillUsing, this)
    };
    c.prototype.isBoxSkillUsing = function(b) {
        if (b == this.config.configId) return ! 0
    };
    c.prototype.countDown = function() {
        doAction(Const.UNLOCK_SKILL, this.config.configId);
        var b = this.config.base + this.config.value * this._lv;
        switch (this.config.configId) {
            case 22002:
                doAction(Const.STOP_MAIN_SKILL_2);
                break;
            case 22003:
                doAction(Const.STOP_MAIN_SKILL_3);
                HeroController.getInstance().mainHero.changeCritPro( - b);
                break;
            case 22004:
                doAction(Const.CHANGE_SPEED, 1 / (b + 1));
                break;
            case 22005:
                doAction(Const.STOP_MAIN_SKILL_4),
                    HeroController.getInstance().mainHero.extraHurt = 1
        }
        doAction(Const.BOX_SKILL_OVER, this.config.configId);
        removeEventsByTarget(this)
    };
    return c
} ();
BoxSkill.prototype.__class__ = "BoxSkill";
var WXUtils = function() {
    function c() {}
    c.showAward = function(b, a, d, c, e) {
        void 0 === a && (a = !0);
        void 0 === d && (d = !1);
        void 0 === c && (c = null);
        void 0 === e && (e = null);
        b && GameMainLayer.instance.showShareLayer(b,
            function() {
                "diamond" == b.type && d && ProxyUtils.getDiamond("share");
                c && c.call(e)
            })
    };
    return c
} ();
WXUtils.prototype.__class__ = "WXUtils";
__extends = this.__extends ||
    function(c, b) {
        function a() {
            this.constructor = c
        }
        for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
        a.prototype = b.prototype;
        c.prototype = new a
    }; (function(c) {
    var b = function(a) {
        function b() {
            a.apply(this, arguments);
            this._scale9Info = null;
            this._otherUrl = !1
        }
        __extends(b, a);
        b.create = function(a, c, h, f) {
            void 0 === c && (c = !1);
            void 0 === h && (h = null);
            void 0 === f && (f = null);
            var k = new b;
            k.startLoad(a, c, h, f);
            return k
        };
        b.prototype.setScale9Info = function(a, b, d, c) {
            this._scale9Info = {
                left: a,
                top: b,
                right: d,
                bottom: c
            }
        };
        b.prototype.startLoad = function(a, b, d, f) {
            this._otherUrl = b;
            this._src = a;
            this._onLoadComplete = d;
            this._onLoadCompleteThisObj = f;
            this._otherUrl ? (b = new c.URLRequest, b.url = a, a = new c.URLLoader, a.dataFormat = c.URLLoaderDataFormat.TEXTURE, a.addEventListener(c.Event.COMPLETE, this.resourceLoadComplete, this), a.load(b)) : RES.getResAsync(this._src, this.resourceLoadComplete, this)
        };
        b.prototype.resourceLoadComplete = function(a) {
            this.texture = this._otherUrl ? a.target.data: a;
            this._scale9Info && (this.scale9Grid = new c.Rectangle(this._scale9Info.left, this._scale9Info.top, this.texture.textureWidth - this._scale9Info.left - this._scale9Info.right, this.texture.textureHeight - this._scale9Info.top - this._scale9Info.bottom));
            this._setSizeDirty();
            this._onLoadComplete && (this._onLoadComplete.call(this._onLoadCompleteThisObj), this._onLoadCompleteThisObj = this._onLoadComplete = null)
        };
        b.prototype._measureBounds = function() {
            return this.texture ? a.prototype._measureBounds.call(this) : c.Rectangle.identity.initialize(0, 0, 0, 0)
        };
        return b
    } (c.Bitmap);
    c.DynamicBitmap = b;
    b.prototype.__class__ = "egret.DynamicBitmap"
})(egret || (egret = {}));
var SoundEffects = function() {
    function c() {
        this._volume = 0.5;
        this._cache = {};
        this._sounds = {};
        this._loadingCache = [];
        var b = new egret.Timer(6E4);
        b.addEventListener(egret.TimerEvent.TIMER, this.dealSoundTimer, this);
        b.start()
    }
    c.prototype.setVolume = function(b) {};
    c.prototype.play = function(b) {
        var a = RES.getRes(b);
        a ? (a.setVolume(this._volume), a ? (a.play(), this._cache[b] && (this._cache[b] = egret.getTimer()), this._sounds[b] = a) : -1 == this._loadingCache.indexOf(b) && (this._loadingCache.push(b), RES.createGroup(b, [b], !0), 1 == this._loadingCache.length && RES.addEventListener(RES.ResourceEvent.GROUP_COMPLETE, this.onResourceLoadComplete, this), RES.loadGroup(b))) : console.log("\u97f3\u6548 " + b + " \u672a\u52a0\u8f7d")
    };
    c.prototype.stop = function(b) {
        this._sounds.hasOwnProperty(b) && this._sounds[b].pause()
    };
    c.prototype.onResourceLoadComplete = function(b) {
        b = b.groupName;
        var a = this._loadingCache.indexOf(b);
        if ( - 1 != a) {
            this._loadingCache.splice(a, 1);
            0 == this._loadingCache.length && RES.removeEventListener(RES.ResourceEvent.GROUP_COMPLETE, this.onResourceLoadComplete, this);
            this._cache[b] = egret.getTimer();
            try {
                var d = RES.getRes(b);
                d.setVolume(this._volume);
                d.play();
                this._sounds[b] = d
            } catch(c) {
                console.log("\u97f3\u6548\u9519\u8bef:" + b)
            }
        }
    };
    c.prototype.dealSoundTimer = function(b) {
        b = egret.getTimer();
        for (var a in this._cache) b - this._cache[a] >= Snd.CLEAR_TIME && (delete this._cache[a], delete this._sounds[a], RES.destroyRes(a))
    };
    return c
} ();
SoundEffects.prototype.__class__ = "SoundEffects";
var Snd = function() {
    function c() {}
    c.init = function() {
        c.ef = new SoundEffects;
        c.bg = new SoundBg;
        c.effectOn = !0;
        c.bgOn = !0;
        c.CLEAR_TIME = 18E4
    };
    c.playEffect = function(b) {
        c.effectOn && c.ef.play(b)
    };
    c.playBg = function(b) {
        c.bgOn && c.bg.play(b)
    };
    c.stopBg = function() {
        c.bg.stop()
    };
    c.setEffectOn = function(b) {
        c.effectOn = b
    };
    c.setBgOn = function(b) {
        c.bgOn = b;
        c.bgOn ? c.playBg("") : c.stopBg()
    };
    return c
} ();
Snd.prototype.__class__ = "Snd";
var SoundBg = function() {
    function c() {
        this._volume = 0.5;
        this._currBg = "";
        this._cache = {};
        this._loadingCache = [];
        var b = new egret.Timer(6E4);
        b.addEventListener(egret.TimerEvent.TIMER, this.dealSoundTimer, this);
        b.start()
    }
    c.prototype.setVolume = function(b) {};
    c.prototype.stop = function() {
        this._currSound && this._currSound.pause();
        this._currSound = null;
        this._currBg = ""
    };
    c.prototype.play = function(b) {
        if (this._currBg != b) {
            this.stop();
            this._currBg = b;
            var a = RES.getRes(b);
            a ? (this._currSound = a, this._currSound.play(!0), this._cache[b] && (this._cache[b] = egret.getTimer())) : -1 == this._loadingCache.indexOf(b) && (this._loadingCache.push(b), RES.createGroup(b, [b], !0), 1 == this._loadingCache.length && RES.addEventListener(RES.ResourceEvent.GROUP_COMPLETE, this.onResourceLoadComplete, this), RES.loadGroup(b))
        }
    };
    c.prototype.onResourceLoadComplete = function(b) {
        b = b.groupName;
        var a = this._loadingCache.indexOf(b);
        if ( - 1 != a && (this._loadingCache.splice(a, 1), 0 == this._loadingCache.length && RES.removeEventListener(RES.ResourceEvent.GROUP_COMPLETE, this.onResourceLoadComplete, this), this._cache[b] = egret.getTimer(), this._currBg == b)) try {
            this._currSound = RES.getRes(b),
                this._currSound.setVolume(this._volume),
                this._currSound.play(!0)
        } catch(d) {
            console.log("\u80cc\u666f\u97f3\u4e50\u9519\u8bef:" + b)
        }
    };
    c.prototype.dealSoundTimer = function(b) {
        b = egret.getTimer();
        for (var a in this._cache) a != this._currBg && b - this._cache[a] >= Snd.CLEAR_TIME && (delete this._cache[a], RES.destroyRes(a))
    };
    return c
} ();
SoundBg.prototype.__class__ = "SoundBg";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    TableView = function(c) {
        function b() {
            c.call(this);
            this._currentIndex = 0;
            this._itemArr = [];
            this.disappearContainer = new egret.DisplayObjectContainer;
            this.addChild(this.disappearContainer)
        }
        __extends(b, c);
        b.prototype.changeDataListOnly = function(a) {
            void 0 === a && (a = []);
            this._dataArr = a
        };
        b.prototype.reloadData = function(a) {
            void 0 === a && (a = []);
            this._dataArr = a || [];
            var b, c;
            a = 0;
            this.direction == Direction.HORIZONTAL ? (b = this._itemWidth * this._dataArr.length, c = this._itemHeight, a = Math.floor( - this._container.x / this._itemWidth)) : (b = this._itemWidth, c = this._itemHeight * this._dataArr.length, a = Math.floor( - this._container.y / this._itemHeight));
            this._initWidth = b;
            this._initHeight = c;
            b = this._itemArr.length;
            for (c = 0; c < b; c++) this.initItem(this._itemArr[c], a + c);
            this._currentIndex = a
        };
        b.prototype.showAnimation = function() {
            if (this.direction == Direction.VERTICAL) for (var a = 0,
                                                               b = 0; a < this._itemArr.length; a++) {
                var c = this._itemArr[a];
                if (c.visible) {
                    var e = egret.Tween.get(c);
                    c.x = this._itemWidth;
                    e.wait(100 * b + 10);
                    e.to({
                            x: 0
                        },
                        200);
                    b++
                }
            }
        };
        b.prototype.hideAnimation = function() {
            if (this.direction == Direction.VERTICAL) for (var a = 0,
                                                               b = 0; a < this._itemArr.length; a++) {
                var c = this._itemArr[a];
                if (c.visible) {
                    var e = egret.Tween.get(c);
                    c.x = 0;
                    e.wait(100 * b + 10);
                    e.to({
                            x: this._itemWidth
                        },
                        200);
                    b++
                }
            }
        };
        b.prototype.setList = function(a, b, c, e, h) {
            this._dataArr = a || [];
            this._delegate = c;
            this._itemWidth = e;
            this._itemHeight = h;
            this.direction = b;
            a = new BaseContainer;
            this.direction == Direction.HORIZONTAL ? (a.width = this._itemWidth * this._dataArr.length, a.height = this._itemHeight, this.setContainer(a, this._itemWidth * this._dataArr.length, this._itemHeight)) : (a.width = this._itemWidth, a.height = this._itemHeight * this._dataArr.length, this.setContainer(a, this._itemWidth, this._itemHeight * this._dataArr.length));
            this.initItemList()
        };
        b.prototype.initItemList = function() {
            if (0 != this._itemWidth && 0 != this._itemHeight && 0 != this._viewWidth && 0 != this._viewHeight) {
                var a = 0,
                    a = this.direction == Direction.HORIZONTAL ? Math.ceil(this._viewWidth / this._itemWidth) + 1 : Math.ceil(this._viewHeight / this._itemHeight) + 1;
                this._itemArr = [];
                for (var b = 0; b < a; b++) {
                    var c = this._delegate.createItemRenderer();
                    this._container.addChild(c);
                    this._itemArr.push(c);
                    this.initItem(c, b)
                }
                this._currentIndex = 0
            }
        };
        b.prototype.getCurrent = function() {
            var a = 0,
                a = this.direction == Direction.HORIZONTAL ? Math.floor( - this._container.x / this._itemWidth) : Math.floor( - this._container.y / this._itemHeight);
            a > this._dataArr.length - this._itemArr.length ? a = this._dataArr.length - this._itemArr.length: 0 > a && (a = 0);
            return a
        };
        b.prototype.moveList = function() {
            var a = this.getCurrent();
            if (! (0 > a || a >= this._dataArr.length - this._itemArr.length + 1 || a == this._currentIndex)) {
                var b = a - this._currentIndex;
                if (Math.abs(b) > this._itemArr.length) for (var g = 0,
                                                                 e = this._itemArr.length; g < e; g++) {
                    var h = this._itemArr[g];
                    this.initItem(h, a + g)
                } else if (0 > b) for (g = 0; g < -b; g++) {
                    h = this._itemArr[this._itemArr.length - 1];
                    this.moveArrayItem(0, this._itemArr.length - 1);
                    var f = a + ( - b - 1) - g;
                    this.initItem(h, f)
                } else for (e = this._itemArr.length, g = 0; g < b; g++) h = this._itemArr[0],
                    this.moveArrayItem(this._itemArr.length - 1, 0),
                    f = b >= e ? a + g: a + e - 1 - (b - 1 - g),
                    this.initItem(h, f);
                this._currentIndex = a
            }
            c.prototype.moveList.call(this)
        };
        b.prototype.moveArrayItem = function(a, b) {
            var c = this._itemArr[b];
            this._itemArr.splice(b, 1);
            a > this._itemArr.length - 1 ? this._itemArr.push(c) : this._itemArr.splice(a, 0, c)
        };
        b.prototype.initItem = function(a, b) {
            b >= this._dataArr.length ? a.visible = !1 : 0 > b ? a.visible = !1 : (a.visible = !0, this._delegate.updateItemRenderer(a, this._dataArr[b], b));
            this.direction == Direction.HORIZONTAL ? a.x = this._itemWidth * b: a.y = this._itemHeight * b
        };
        return b
    } (ScrollView);
TableView.prototype.__class__ = "TableView";
var ProxyCache = function() {
    function c() {
        this._cacheData = {}
    }
    Object.defineProperty(c, "instance", {
        get: function() {
            this._instance || (this._instance = new c);
            return this._instance
        },
        enumerable: !0,
        configurable: !0
    });
    c.prototype.setCache = function(b) {
        var a = b.params;
        if (a.hasOwnProperty("cache") && !0 === a.cache) {
            var a = b.responseData,
                d = b.getParamByName("mod");
            b = b.getParamByName("do");
            this._cacheData.hasOwnProperty(d) || (this._cacheData[d] = {});
            this._cacheData[d][b] = a;
            doAction("Cache." + d + "." + b, a);
            doAction("cache_proxy_data", d + "." + b)
        }
    };
    c.prototype.getCache = function(b, a) {
        void 0 === b && (b = null);
        void 0 === a && (a = null);
        if (null == b) return this._cacheData;
        if (this.isCache(b, a)) {
            var d = this.formatParmas(b, a);
            return this._cacheData[d[0]][d[1]]
        }
        return null
    };
    c.prototype.formatParmas = function(b, a) {
        void 0 === a && (a = null);
        if (null == a) {
            var d = b.split(".");
            b = d[0];
            a = d[1]
        }
        return [b, a]
    };
    c.prototype.isCache = function(b, a) {
        void 0 === a && (a = null);
        var d = this.formatParmas(b, a);
        return this._cacheData.hasOwnProperty(d[0]) && this._cacheData[d[0]].hasOwnProperty(d[1])
    };
    c.setCache = function(b) {
        c.instance.setCache(b)
    };
    c.getCache = function(b) {
        void 0 === b && (b = null);
        return c.instance.getCache(b)
    };
    c.isCache = function(b, a) {
        void 0 === a && (a = null);
        return c.instance.isCache(b, a)
    };
    c._instance = null;
    return c
} ();
ProxyCache.prototype.__class__ = "ProxyCache";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    ProxyEvent = function(c) {
        function b(a, b, g, e) {
            c.call(this, a, g, e);
            this._responseData = b.responseData;
            this._errorCode = b.errorCode;
            this._errorMessage = b.errorMessage;
            this._isRequestSucceed = b.isRequestSucceed;
            this._isResponseSucceed = b.isResponseSucceed
        }
        __extends(b, c);
        Object.defineProperty(b.prototype, "responseData", {
            get: function() {
                return this._responseData
            },
            enumerable: !0,
            configurable: !0
        });
        Object.defineProperty(b.prototype, "errorCode", {
            get: function() {
                return this._errorCode
            },
            enumerable: !0,
            configurable: !0
        });
        Object.defineProperty(b.prototype, "errorMessage", {
            get: function() {
                return this._errorMessage
            },
            enumerable: !0,
            configurable: !0
        });
        Object.defineProperty(b.prototype, "isResponseSucceed", {
            get: function() {
                return this._isResponseSucceed
            },
            enumerable: !0,
            configurable: !0
        });
        Object.defineProperty(b.prototype, "isRequestSucceed", {
            get: function() {
                return this._isRequestSucceed
            },
            enumerable: !0,
            configurable: !0
        });
        b.CANCEL = "cancel";
        b.REQUEST_SUCCEED = "requestSucceed";
        b.REQUEST_FAIL = "fail";
        b.RESPONSE_SUCCEED = "respnseSucceed";
        b.RESPONSE_ERROR = "error";
        b.ERROR = "global_error";
        b.TIME_OUT = "timeout";
        return b
    } (egret.Event);
ProxyEvent.prototype.__class__ = "ProxyEvent";
var ProxyUpdate = function() {
    function c() {}
    Object.defineProperty(c, "instance", {
        get: function() {
            null == this._instance && (this._instance = new c);
            return this._instance
        },
        enumerable: !0,
        configurable: !0
    });
    c.update = function(b, a) {
        c.instance.update(b, a)
    };
    c.prototype.isArray = function(b) {
        return b instanceof Array
    };
    c.prototype.isObject = function(b) {
        return - 1 < b.toString().indexOf("object")
    };
    c.prototype.isNumeric = function(b) {
        return parseFloat(b).toString() == b.toString()
    };
    c.prototype.isNormal = function(b) {
        var a = -1 < b.indexOf("@"),
            d = -1 < b.indexOf(".");
        b = -1 < b.indexOf("_");
        return ! a && !d && !b
    };
    c.prototype.isAddToArray = function(b) {
        return "@a" == b
    };
    c.prototype.isRemoveToArray = function(b) {
        b = b.split("_");
        return 3 >= b.length && "@d" == b[0]
    };
    c.prototype.isFilter = function(b) {
        return "@f" == b.split("_")[0]
    };
    c.prototype._updateObject = function(b, a, d) {
        b = b.split(".");
        "@a" == b[0] || "@s" == b[0] ? this.isArray(d) ? d[parseInt(b[1])] = a: d[b[1]] = a: "@d" == b[0] && delete d[b[1]]
    };
    c.prototype._getFilterObject = function(b, a) {
        if (a) {
            var d = b.split("_");
            if (2 == d.length && "@f" == d[0] && this.isArray(a)) return d = parseInt(d[1]),
                a[d];
            if (3 == d.length && "@f" == d[0] && this.isArray(a)) for (var c = d[1], e = d[2], h = 0; h < a.length; h++) {
                var f = a[h];
                if (3 == d.length && this.isObject(f)) {
                    var k = f[c];
                    if (k && ("@" == e[0] && (e = e.replace("@", "")), e == k)) return f
                }
            }
        }
        return null
    };
    c.prototype._addObjectToArray = function(b, a) {
        if (this.isArray(a)) for (var d = 0; d < a.length; d++) b.push(a[d]);
        else b.push(a)
    };
    c.prototype._removeObjectFromArray = function(b, a, d) {
        a = a.split("_");
        if (3 >= a.length && "@d" == a[0] && this.isArray(b)) for (var c = b.length - 1; 0 <= c; c--) {
            var e = b[c];
            if (3 == a.length) {
                if (e.hasOwnProperty(a[1])) {
                    var h = a[2];
                    "@" == h[0] && (h = h.replace("@", ""));
                    h == e[a[1]] && b.splice(c, 1)
                }
            } else 2 == a.length && e.hasOwnProperty(a[1]) ? d == e[a[1]] && b.splice(c, 1) : 1 == a.length && d == e && b.splice(c, 1)
        }
    };
    c.prototype.update = function(b, a) {
        var d = b.responseData.c,
            c;
        for (c in d) {
            var e = d[c];
            console.log(c);
            var h = c.split("."),
                f = h[0],
                h = h[1];
            a.hasOwnProperty(f) && a[f].hasOwnProperty(h) && (this._update(a[f][h], e), doAction("Change." + f + ".pdo", e), doAction("cache_change", f + "." + h, e))
        }
    };
    c.prototype._update = function(b, a) {
        if (b && a && this.isObject(a)) for (var d in a) {
            var c = a[d];
            if (this.isNormal(d) && this.isObject(c)) b.hasOwnProperty(d) && this._update(b[d], c);
            else if (this.isNormal(d) && this.isNumeric(c)) b[d] += c;
            else if (this.isNormal(d)) b[d] = c;
            else if (this.isAddToArray(d)) this._addObjectToArray(b, c);
            else if (this.isRemoveToArray(d)) this._removeObjectFromArray(b, d, c);
            else if (this.isFilter(d)) {
                var e = this._getFilterObject(d, b);
                e && this._update(e, c)
            } else this._updateObject(d, c, b)
        }
    };
    return c
} ();
ProxyUpdate.prototype.__class__ = "ProxyUpdate";
var cacheFunc = function() {
    function c() {}
    c.getDeleteName = function(b) {
        return b.split("_")[1]
    };
    c.getDeleteValue = function(b, a) {
        var d = b.split("_");
        return 3 == d.length ? d[2].replace("@", "") : a
    };
    c.fields = {
        add: "@a",
        delete: "@d"
    };
    return c
} ();
cacheFunc.prototype.__class__ = "cacheFunc";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    SingleProxy = function(c) {
        function b(a) {
            void 0 === a && (a = {});
            c.call(this, a)
        }
        __extends(b, c);
        b.prototype.getParamString = function() {
            return JSON.stringify(this._params)
        };
        return b
    } (Proxy);
SingleProxy.prototype.__class__ = "SingleProxy";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    HeroItemView = function(c) {
        function b() {
            c.call(this);
            this.initView()
        }
        __extends(b, c);
        b.prototype.initView = function() {
            this._bg = ResourceUtils.createBitmapFromSheet("hero_item_bg", "commonRes");
            this.addChild(this._bg);
            this._touchArea = new egret.Sprite;
            this._touchArea.width = 430;
            this._touchArea.height = 100;
            this._touchArea.touchEnabled = !0;
            this.addChild(this._touchArea);
            this._touchArea.addEventListener(egret.TouchEvent.TOUCH_TAP, this.heroClick, this);
            this._evoMask = ResourceUtils.createBitmapFromSheet("gold_border");
            this.addChild(this._evoMask);
            this._evoMask.x = 18;
            this._evoMask.y = 4;
            this._iconContainer = new egret.DisplayObjectContainer;
            this._iconContainer.x = 10;
            this._iconContainer.y = -2;
            this.addChild(this._iconContainer);
            this._skillIconContainer = new egret.DisplayObjectContainer;
            this.addChild(this._skillIconContainer);
            this._skillIconContainer.y = 56;
            this._skillIconContainer.x = 110;
            this._staticContainer = new egret.DisplayObjectContainer;
            this.addChild(this._staticContainer);
            this._staticContainer.x = 20;
            this._staticContainer.y = 4;
            this._dpsLabel = new egret.TextField;
            this._dpsLabel.x = 260;
            this._dpsLabel.y = 32;
            this._dpsLabel.size = 20;
            this._dpsLabel.text = "DPS:";
            this._dpsLabel.textColor = 16777215;
            this._staticContainer.addChild(this._dpsLabel);
            this._desLabel = new egret.TextField;
            this._staticContainer.addChild(this._desLabel);
            this._desLabel.x = 310;
            this._desLabel.y = 32;
            this._desLabel.size = 20;
            this._desLabel.textColor = 16777215;
            var a = new egret.TextField;
            a.x = 87;
            a.y = 32;
            a.size = 20;
            a.text = "Lv.";
            a.textColor = 16777215;
            this._staticContainer.addChild(a);
            this._lvLabel = new egret.TextField;
            this._staticContainer.addChild(this._lvLabel);
            this._lvLabel.x = 120;
            this._lvLabel.y = 32;
            this._lvLabel.textColor = 16777215;
            this._lvLabel.size = 20;
            this._nameLabel = new egret.TextField;
            this._staticContainer.addChild(this._nameLabel);
            this._nameLabel.x = 87;
            this._nameLabel.y = 5;
            this._nameLabel.size = 20;
            this._nameLabel.textColor = 2296834;
            this._staticContainer.cacheAsBitmap = !0;
            this._button = new LevelUpButton("hero");
            this._button.x = 520;
            this._button.y = 50;
            this._button.setCallBack(this, this.onClick);
            this.addChild(this._button);
            this._deathMask = ResourceUtils.createBitmapFromSheet("red_mask");
            this.addChild(this._deathMask);
            this._deathMask.visible = !1;
            addAction(Const.CHANGE_COIN, this.changeCoin, this)
        };
        b.prototype.changeCoin = function() {
            this._button.refreshData(this._model)
        };
        b.prototype.heroClick = function() {
            this._model.isDead() ? GameMainLayer.instance.showDieLayer(this._model) : GameMainLayer.instance.showHeroDetailLayer(this._model)
        };
        b.prototype.onClick = function(a, b) {
            if (1 == a) this.levelUp1();
            else if (10 == a) this.levelUp10();
            else if (100 == a) this.levelUp100();
            else if (1E3 == a) this.unLockSkill();
            else throw Error("\u4f60\u6309\u7684\u5565");
            DataCenter.getInstance().reduceSc(b);
            this._button.refreshAllButton();
            doAction(Const.UPDATE_HERO)
        };
        b.prototype.refreshView = function(a, b) {
            this._model = a;
            this._idx = b;
            this._evoMask.visible = this._model.isEvolution();
            this._button.removeNewTip();
            this.refreshSkillIcon();
            this.refreshLabel(!1);
            this._button.refreshData(this._model);
            this._model.isDead() ? this._deathMask.visible = !0 : this._deathMask.visible = !1
        };
        b.prototype.refreshSkillIcon = function() {
            this._skillIconContainer.removeChildren();
            for (var a = this._model.skillList,
                     b = 0; b < a.length; b++) {
                var c = ResourceUtils.createBitmapFromSheet(a[b].getRes(), "iconRes");
                c.scaleX = c.scaleY = 0.4;
                c.x = 40 * b;
                this._skillIconContainer.addChild(c)
            }
            if (7 > a.length) {
                var a = this._model.config.skill.split(",")[b],
                    e = RES.getRes("skillConfig"),
                    c = ResourceUtils.createBitmapFromSheet("icon_skill_" + e[a].iconID, "iconRes");
                c.scaleX = c.scaleY = 0.4;
                c.x = 40 * b;
                this._skillIconContainer.addChild(c);
                c = ResourceUtils.createBitmapFromSheet("head_mask");
                c.alpha = 0.9;
                c.scaleX = c.scaleY = 0.4;
                c.x = 40 * b;
                this._skillIconContainer.addChild(c);
                c = new egret.TextField;
                c.size = 6;
                c.x = 40 * b;
                c.y = 20;
                c.text = 1E3 < this._model.lv ? "Lv." + (e[a].level + 1E3) : "Lv." + e[a].level;
                this._skillIconContainer.addChild(c)
            }
            this._skillIconContainer.cacheAsBitmap = !0
        };
        b.prototype.showAnimation = function() {
            var a = new Movie("hero_upgrade_json", "hero_upgrade");
            a.play("2");
            a.x = 50;
            a.y = 48;
            a.scaleX = a.scaleY = 1.5;
            this._iconContainer.addChild(a)
        };
        b.prototype.refreshLabel = function(a) {
            void 0 === a && (a = !0);
            this._lvLabel.text = this._model.lv.toString();
            this._desLabel.text = "" + this._model.dps;
            this._nameLabel.text = this._model.config.name;
            this._iconContainer.removeChildren();
            var b = ResourceUtils.createBitmapFromSheet(this._model.getRes(), "iconRes");
            b.x = 15;
            b.y = 14;
            this._iconContainer.addChild(b);
            0 == this._model.lv ? (b = ResourceUtils.createBitmapFromSheet("head_mask"), b.x = 13, b.y = 12, this._iconContainer.addChild(b), this._dpsLabel.text = "\u672a\u62db\u52df", this._desLabel.visible = !1) : (this._dpsLabel.text = "DPS:", this._desLabel.visible = !0);
            b = ResourceUtils.createBitmapFromSheet("icon_decorate");
            b.x = 2;
            b.y = 25;
            this._iconContainer.addChild(b);
            a && GameUtils.preloadAnimation("hero_upgrade_json", this.showAnimation, this)
        };
        b.prototype.levelUp1 = function() {
            this._model.lv += 1;
            doAction(Const.HERO_LIST_CHANGE, this._model, this._idx);
            1 == this._model.lv ? (doAction(Guide.BUY_HERO), DataCenter.getInstance().addHero(this._model)) : 1001 == this._model.lv ? DataCenter.getInstance().evolutionHero(this._model) : DataCenter.getInstance().levelUpHero(this._model, 1);
            this.refreshLabel()
        };
        b.prototype.unLockSkill = function() {
            DataCenter.getInstance().unlockSkill(this._model, this._model.nextSkill.configId);
            this.refreshLabel()
        };
        b.prototype.levelUp10 = function() {
            this._model.lv += 10;
            doAction(Const.HERO_LIST_CHANGE, this._model, this._idx);
            DataCenter.getInstance().levelUpHero(this._model, 10);
            this.refreshLabel()
        };
        b.prototype.levelUp100 = function() {
            this._model.lv += 100;
            doAction(Const.HERO_LIST_CHANGE, this._model, this._idx);
            DataCenter.getInstance().levelUpHero(this._model, 100);
            this.refreshLabel()
        };
        b.prototype.dispose = function() {
            c.prototype.dispose.call(this);
            this._touchArea.removeEventListener(egret.TouchEvent.TOUCH_TAP, this.heroClick, this);
            removeEventsByTarget(this)
        };
        return b
    } (BaseContainer);
HeroItemView.prototype.__class__ = "HeroItemView";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    PaymentItemView = function(c) {
        function b() {
            c.call(this);
            this._icon = null;
            this.init()
        }
        __extends(b, c);
        b.prototype.init = function() {
            var a = ResourceUtils.createBitmapFromSheet("hero_item_bg", "commonRes");
            this.addChild(a);
            this._iconContainer = new egret.DisplayObjectContainer;
            this.addChild(this._iconContainer);
            this._container = new egret.DisplayObjectContainer;
            this._iconContainer.addChild(this._container);
            this._container.x = 25;
            this._container.y = 11;
            a = ResourceUtils.createBitmapFromSheet("icon_decorate");
            a.x = 13;
            a.y = 25;
            this._iconContainer.addChild(a);
            this._title = new egret.TextField;
            this._iconContainer.addChild(this._title);
            this._title.x = 107;
            this._title.size = 20;
            this._title.y = 10;
            this._title.textColor = 2495759;
            this._description = new egret.TextField;
            this._iconContainer.addChild(this._description);
            this._description.x = 107;
            this._description.y = 40;
            this._description.size = 18;
            this._iconContainer.cacheAsBitmap = !0;
            this._button = new PaymentButton;
            this._button.addOnClick(this.click, this);
            this._button.useZoomOut(!0);
            this.addChild(this._button);
            this._button.x = 518;
            this._button.y = 58;
            this._currencyIcon = ResourceUtils.createBitmapFromSheet("gold_icon");
            this._currencyIcon.anchorX = this._currencyIcon.anchorY = 0.45;
            this._currencyIcon.scaleX = this._currencyIcon.scaleY = 0.45;
            this._container.addChild(this._currencyIcon);
            this._currencyIcon.x = 505;
            this._currencyIcon.y = 14;
            this._iconContainer.addChild(this._currencyIcon);
            this._price = new egret.TextField;
            this._price.text = "0";
            this._price.size = 20;
            this._container.addChild(this._price);
            this._price.anchorX = 1;
            this._price.x = 550;
            this._price.y = 4;
            this._iconContainer.addChild(this._price)
        };
        b.prototype.click = function() {
            var a = this;
            PaymentFactory.getInstance().effect(this._model,
                function() {
                    a._model.countdown();
                    a.refreshView(a._model, a._idx)
                })
        };
        b.prototype.refreshView = function(a, b) {
            this._model = a;
            this._idx = b;
            GameUtils.removeFromParent(this._icon);
            GameUtils.removeAllChildren(this._container);
            this._icon = ResourceUtils.createBitmapFromSheet(this._model.icon, "iconRes");
            this._container.addChild(this._icon);
            this._title.text = this._model.title;
            this._description.textFlow = (new egret.HtmlTextParser).parser(this._model.getFormatDescription());
            this._description.textColor = this._model.descColor;
            this._button.refresh(this._model);
            this._price.text = this._model.getFormatValue();
            this._currencyIcon.visible = this._model.mode == PaymentMode.USE || this._model.mode == PaymentMode.SHARE;
            removeEventsByTarget(this);
            "coin" == this._model.type && addAction(Const.BOSS_KILLED, this.refreshDescription, this)
        };
        b.prototype.refreshDescription = function() {
            this._description.textFlow = (new egret.HtmlTextParser).parser(this._model.getFormatDescription());
            this._price.text = this._model.getFormatValue()
        };
        return b
    } (BaseContainer);
PaymentItemView.prototype.__class__ = "PaymentItemView";
var PaymentMode = function() {
    function c() {}
    c.getNormal = function(b) {
        return this._normals[b]
    };
    c.getDisabled = function(b) {
        return "hero_lvup_disable"
    };
    c.getText = function(b) {
        if (b == this.USE) return "\u4f7f\u7528\u9053\u5177";
        if (b == this.SHARE) return "\u5206\u4eab";
        if (b == this.BUY) return "\u8d2d\u4e70\u94bb\u77f3"
    };
    c.USE = "use";
    c.BUY = "buy";
    c.SHARE = "share";
    c._normals = {
        use: "hero_lvup_btn",
        buy: "hero_lvup_btn",
        share: "hero_lvup_btn"
    };
    return c
} ();
PaymentMode.prototype.__class__ = "PaymentMode";
var PaymentButton = function(c) {
    function b() {
        c.call(this);
        this._timeid = this._tip = this._oldMode = null;
        this.init();
        this.anchorX = this.anchorY = 0.5;
        this.width = 161;
        this.height = 68
    }
    __extends(b, c);
    b.prototype.init = function() {
        this._container = new egret.DisplayObjectContainer;
        this._label = new egret.TextField;
        this._label.text = "\u4f7f\u7528\u6280\u80fd";
        this._label.size = 20;
        this._label.anchorX = this._label.anchorY = 0.5;
        this._container.addChild(this._label);
        this._label.x = 86;
        this._label.y = 30;
        this._label.textColor = 2495759;
        this.addChild(this._container);
        this._container.cacheAsBitmap = !0
    };
    b.prototype.refreshView = function() {
        this._model.mode != this._oldMode && (this._oldMode = this._model.mode, GameUtils.removeFromParent(this._normal), GameUtils.removeFromParent(this._disabled), this._normal = ResourceUtils.createBitmapFromSheet(PaymentMode.getNormal(this._model.mode)), this._container.addChildAt(this._normal, 0), this._disabled = ResourceUtils.createBitmapFromSheet(PaymentMode.getDisabled(this._model.mode)), this._disabled.visible = !1, this._container.addChildAt(this._disabled, 0))
    };
    b.prototype.refreshTip = function() {
        this._tip && egret.Tween.removeTweens(this._tip);
        GameUtils.removeFromParent(this._tip);
        this._tip = null;
        this._model.tip && (this._tip = ResourceUtils.createBitmapFromSheet(this._model.tip), this._tip.anchorX = this._anchorY = 0.5, this.addChild(this._tip), this._tip.scaleX = this._tip.scaleY = 0.7, this._tip.x = 20, this._tip.y = -5, egret.Tween.get(this._tip, {
            loop: !0
        }).to({
                scaleX: 0.9,
                scaleY: 0.9
            },
            500).to({
                scaleX: 0.7,
                scaleY: 0.7
            },
            500))
    };
    b.prototype.refreshTime = function() {
        0 != this._model.time ? (this.timeRefresh(), this._timeid = Time.setInterval(this.timeRefresh, this, 1E3)) : (Time.clearTimeout(this._timeid), this._label.text = PaymentMode.getText(this._model.mode));
        this.setEnabled(0 == this._model.time)
    };
    b.prototype.timeRefresh = function() {
        0 == this._model.time ? this.refreshTime() : this._label.text = GameUtils.formatShortTime(this._model.time)
    };
    b.prototype.setEnabledState = function() {
        this._normal.visible = this._enabled;
        this._disabled.visible = !this._enabled
    };
    b.prototype.refresh = function(a) {
        this._model = a;
        this.refreshView();
        this.refreshTip();
        this.refreshTime()
    };
    return b
} (SimpleButton);
PaymentButton.prototype.__class__ = "PaymentButton";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    SkillItemView = function(c) {
        function b() {
            c.call(this);
            this._opened = !1;
            this.initView()
        }
        __extends(b, c);
        b.prototype.initView = function() {
            this._bg = ResourceUtils.createBitmapFromSheet("hero_item_bg", "commonRes");
            this.addChild(this._bg);
            this._touchArea = new egret.Sprite;
            this._touchArea.width = 430;
            this._touchArea.height = 100;
            this._touchArea.touchEnabled = !0;
            this.addChild(this._touchArea);
            this._touchArea.addEventListener(egret.TouchEvent.TOUCH_TAP, this.heroClick, this);
            this._iconContainer = new egret.DisplayObjectContainer;
            this._iconContainer.x = 10;
            this._iconContainer.y = -2;
            this.addChild(this._iconContainer);
            this._staticContainer = new egret.DisplayObjectContainer;
            this.addChild(this._staticContainer);
            this._staticContainer.x = 20;
            this._staticContainer.y = 4;
            this._lvLabel1 = new egret.TextField;
            this._staticContainer.addChild(this._lvLabel1);
            this._lvLabel1.x = 87;
            this._lvLabel1.y = 35;
            this._lvLabel1.size = 20;
            this._lvLabel1.text = "Lv.";
            this._lvLabel1.textColor = 16777215;
            this._lvLabel = new egret.TextField;
            this._staticContainer.addChild(this._lvLabel);
            this._lvLabel.x = 117;
            this._lvLabel.y = 35;
            this._lvLabel.size = 20;
            this._lvLabel.textColor = 16777215;
            this._nameLabel = new egret.TextField;
            this._staticContainer.addChild(this._nameLabel);
            this._nameLabel.x = 87;
            this._nameLabel.y = 5;
            this._nameLabel.size = 20;
            this._nameLabel.textColor = 2296834;
            this._desLabel = new egret.TextField;
            this._staticContainer.addChild(this._desLabel);
            this._desLabel.x = 87;
            this._desLabel.y = 60;
            this._desLabel.size = 20;
            this._desLabel.textColor = 16777215;
            this._normalAttackLabel = new egret.TextField;
            this._staticContainer.addChild(this._normalAttackLabel);
            this._normalAttackLabel.x = 175;
            this._normalAttackLabel.y = 60;
            this._normalAttackLabel.size = 20;
            this._normalAttackLabel.textColor = 16777215;
            this._staticContainer.cacheAsBitmap = !0;
            this._button = new LevelUpButton("skill");
            this._button.x = 520;
            this._button.y = 50;
            this._button.setCallBack(this, this.onClick);
            this.addChild(this._button);
            addAction(Const.CHANGE_COIN, this.changeCoin, this)
        };
        b.prototype.changeCoin = function() {
            this._button.refreshData(this._data, 0 == this._idx)
        };
        b.prototype.heroClick = function() {
            GameMainLayer.instance.showMainHeroDetailLayer(HeroController.getInstance().mainHero)
        };
        b.prototype.onClick = function(a, b) {
            if (1 == a) this.levelUp1();
            else if (10 == a) this.levelUp10();
            else if (100 == a) this.levelUp100();
            else throw Error("\u4f60\u6309\u7684\u5565");
            b && DataCenter.getInstance().reduceSc(b);
            this._button.refreshAllButton()
        };
        b.prototype.levelUp1 = function() {
            7 != this._idx && (this._data.lv += 1);
            doAction(Const.MAIN_HERO_SKILL_CHANGE, this._data, this._idx);
            0 == this._idx ? (doAction(Guide.UP_HERO), HeroController.getInstance().mainHero.lv += 1, DataCenter.getInstance().levelUpHero(HeroController.getInstance().mainHero, 1)) : 7 == this._idx ? GameMainLayer.instance.showResetGameLayer() : (doAction(Guide.UNLOCK_MAIN_SKILL), DataCenter.getInstance().mainSkillUp(this._data.configId, 1));
            this.refreshLabel()
        };
        b.prototype.levelUp10 = function() {
            this._data.lv += 10;
            doAction(Const.MAIN_HERO_SKILL_CHANGE, this._data, this._idx);
            0 == this._idx ? (HeroController.getInstance().mainHero.lv += 10, DataCenter.getInstance().levelUpHero(HeroController.getInstance().mainHero, 10)) : DataCenter.getInstance().mainSkillUp(this._data.configId, 10);
            this.refreshLabel()
        };
        b.prototype.levelUp100 = function() {
            this._data.lv += 100;
            doAction(Const.MAIN_HERO_SKILL_CHANGE, this._data, this._idx);
            0 == this._idx ? (HeroController.getInstance().mainHero.lv += 100, DataCenter.getInstance().levelUpHero(HeroController.getInstance().mainHero, 100)) : DataCenter.getInstance().mainSkillUp(this._data.configId, 100);
            this.refreshLabel()
        };
        b.prototype.refreshLabel = function(a) {
            void 0 === a && (a = !0);
            this._opened = !1;
            this._lvLabel.visible = !0;
            this._lvLabel1.visible = !0;
            if (0 == this._idx) {
                var b = HeroController.getInstance().mainHero;
                this._lvLabel.text = "" + b.lv;
                this._nameLabel.text = this._data.name;
                b = b.tapDamage;
                this._desLabel.text = "\u70b9\u51fb\u4f24\u5bb3:";
                this._normalAttackLabel.text = "" + b
            } else 7 == this._idx ? (this._nameLabel.text = this._data.name, this._desLabel.text = this._data.des, this._lvLabel.visible = !1, this._lvLabel1.visible = !1, this._opened = 600 <= HeroController.getInstance().mainHero.lv) : (this._nameLabel.text = this._data.config.name, 0 == this._data.lv ? this._lvLabel.text = "0": (this._lvLabel.text = "" + this._data.lv, this._opened = !0), this._desLabel.text = this._data.getDes(), this._normalAttackLabel.text = "");
            this._iconContainer.removeChildren();
            0 == this._idx ? b = ResourceUtils.createBitmapFromSheet("icon_hero_skill_1", "iconRes") : (b = 2 * this._idx, !1 == this._opened && (b += 1), b = ResourceUtils.createBitmapFromSheet("icon_hero_skill_" + b, "iconRes"));
            b.x = 15;
            b.y = 14;
            this._iconContainer.addChild(b);
            b = ResourceUtils.createBitmapFromSheet("icon_decorate");
            b.x = 2;
            b.y = 25;
            this._iconContainer.addChild(b);
            a && 7 != this._idx && GameUtils.preloadAnimation("hero_upgrade_json", this.showAnimation, this)
        };
        b.prototype.showAnimation = function() {
            var a = new Movie("hero_upgrade_json", "hero_upgrade");
            a.play("2");
            a.x = 50;
            a.y = 48;
            a.scaleX = a.scaleY = 1.5;
            this._iconContainer.addChild(a)
        };
        b.prototype.refreshView = function(a, b) {
            this._data = a;
            this._idx = b;
            this.refreshLabel(!1);
            this._button.removeNewTip();
            this._button.refreshData(this._data, 0 == this._idx || 7 == this._idx, this._idx)
        };
        b.prototype.dispose = function() {
            c.prototype.dispose.call(this);
            removeEventsByTarget(this);
            this._touchArea.removeEventListener(egret.TouchEvent.TOUCH_TAP, this.heroClick, this)
        };
        return b
    } (BaseContainer);
SkillItemView.prototype.__class__ = "SkillItemView";
var AchieveModel = function() {
    function c(b) {
        this._config = b
    }
    c.prototype.getSubConf = function() {
        var b = Math.min(this._config.levels.length - 1, this.index);
        return this._config.levels[b]
    };
    c.prototype.isMax = function() {
        return this.index >= this._config.levels.length
    };
    Object.defineProperty(c.prototype, "index", {
        get: function() {
            return parseInt(DataCenter.getInstance().achieve.getValue(this.type))
        },
        enumerable: !0,
        configurable: !0
    });
    c.prototype.canAward = function() {
        var b = new NumberModel;
        b.add(this.current);
        return b.compare(this.total) && !this.isMax()
    };
    Object.defineProperty(c.prototype, "id", {
        get: function() {
            return this._config.id
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "current", {
        get: function() {
            return DataCenter.getInstance().statistics.getValue(this.type)
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "total", {
        get: function() {
            return this.getSubConf().progress
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "award", {
        get: function() {
            return this.getSubConf().award
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "name", {
        get: function() {
            var b = this.getSubConf();
            return b.name.replace("{0}", b.progress)
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "pic", {
        get: function() {
            return this._config.icon
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "type", {
        get: function() {
            return this._config.type
        },
        enumerable: !0,
        configurable: !0
    });
    return c
} ();
AchieveModel.prototype.__class__ = "AchieveModel";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    CoinContainer = function(c) {
        function b(a, b) {
            void 0 === b && (b = 1);
            c.call(this);
            this._context = this._selector = this._itemsTouch = null;
            this.updated = this._isOpen = !1;
            this.init(a, b)
        }
        __extends(b, c);
        Object.defineProperty(b.prototype, "num", {
            get: function() {
                return this._num
            },
            enumerable: !0,
            configurable: !0
        });
        Object.defineProperty(b.prototype, "type", {
            get: function() {
                return this._type
            },
            enumerable: !0,
            configurable: !0
        });
        b.prototype.showAnimation = function() {
            var a = GameUtils.random( - 90, 90),
                b = new DropAnimation(500, 2 * a);
            b.run(this);
            b.setComplete(function() { (new DropAnimation1(100, a / 2)).run(this)
                },
                this)
        };
        b.prototype.showAnimation1 = function() {
            var a = GameUtils.random( - 90, 90); (new DropAnimation2(500, 2 * a)).run(this)
        };
        b.prototype.init = function(a, b) {
            this._num = a;
            this._type = b;
            this._itemsTouch = new ItemsTouchManager;
            this._itemsTouch.addItems(this);
            this._itemsTouch.setController(new TouchController(this.onClickHandler, this));
            this._coin = 2 == this._type ? ResourceUtils.createBitmapFromSheet("gold_icon", "commonRes") : 3 == this._type ? ResourceUtils.createBitmapFromSheet("holy_icon") : 8 == this._type ? ResourceUtils.createBitmapFromSheet("suprise") : ResourceUtils.createBitmapFromSheet("coin_icon", "commonRes");
            this._coin.anchorX = this._coin.anchorY = 0.5;
            this._coin.scaleX = this._coin.scaleY = 0.7;
            this.addChild(this._coin);
            Time.setTimeout(this.autoCollect, this, 2E3)
        };
        b.prototype.cleanUp = function() {
            this._isOpen = !0
        };
        b.prototype.autoCollect = function() { ! 1 == this._isOpen && this.open(0)
        };
        b.prototype.open = function(a) {
            this._isOpen = !0;
            Time.setTimeout(function() {
                    AnimationClear.clear(this);
                    doAction(Const.FLY_COIN, this)
                },
                this, a)
        };
        b.prototype.onClickHandler = function(a, b) {
            this._isOpen || 3 == a && this._selector && this._selector.call(this._context, this, 0)
        };
        b.prototype.setCallback = function(a, b) {
            void 0 === b && (b = null);
            this._selector = a;
            this._context = b
        };
        b.prototype.dispose = function() {
            c.prototype.dispose.call(this);
            this._itemsTouch.dispose()
        };
        return b
    } (BaseContainer);
CoinContainer.prototype.__class__ = "CoinContainer";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    GameSetLayer = function(c) {
        function b() {
            c.call(this);
            this.initView()
        }
        __extends(b, c);
        b.prototype.initView = function() {
            var a = ResourceUtils.createBitmapByName("mask");
            this.addChild(a);
            a.touchEnabled = !0;
            a.addEventListener(egret.TouchEvent.TOUCH_TAP, this.onMaskHandler, this);
            a = ResourceUtils.createBitmapByName("set_bg");
            this.addChild(a);
            a.x = 34;
            a.y = 0;
            var b = new SimpleButton,
                a = ResourceUtils.createBitmapFromSheet("close_btn", "commonRes");
            b.addChild(a);
            b.addOnClick(this.onClickHandler, this);
            b.x = 543;
            b.y = 60;
            b.useZoomOut(!0);
            this.addChild(b);
            this._container = new egret.DisplayObjectContainer;
            this.addChild(this._container);
            this._container.y = -20;
            a = ResourceUtils.createBitmapFromSheet("set_btn_bg");
            this._container.addChild(a);
            a.x = 80;
            a.y = 155;
            this._musicBtn = new MusicButton;
            this._musicBtn.initButton("music_on", "music_off", "\u97f3\u4e50:{0}");
            this._container.addChild(this._musicBtn);
            this._musicBtn.addOnClick(this.onMusicClickHandler, this);
            this._musicBtn.useZoomOut(!0);
            this._musicBtn.x = 150;
            this._musicBtn.y = 250;
            a = SoundManager.getInstance().getMusicStatus();
            this._musicBtn.setButtonStatus(a);
            this._musicLabel = new egret.TextField;
            this._container.addChild(this._musicLabel);
            this._musicLabel.x = 102;
            this._musicLabel.y = 305;
            this._musicLabel.text = "\u97f3\u4e50:" + ["\u5173", "\u5f00"][a - 1];
            a = ResourceUtils.createBitmapFromSheet("set_btn_bg");
            this._container.addChild(a);
            a.x = 250;
            a.y = 155;
            b = new SimpleButton;
            b.x = 320;
            b.y = 250;
            b.anchorX = b.anchorY = 0.5;
            a = ResourceUtils.createBitmapFromSheet("StatisticsIcon", "iconRes");
            b.addChild(a);
            b.useZoomOut(!0);
            b.addOnClick(this.statisticsHandler, this);
            this._container.addChild(b);
            a = new egret.TextField;
            a.text = "\u7edf\u8ba1\u6570\u636e";
            a.size = 26;
            a.x = 265;
            a.y = 305;
            this._container.addChild(a);
            PlatformFactory.getPlatform().name == PlatformTypes.WEIXIN && (a = ResourceUtils.createBitmapFromSheet("set_btn_bg"), this._container.addChild(a), a.x = 420, a.y = 155, a = new SimpleButton, a.x = 490, a.y = 250, a.anchorX = a.anchorY = 0.5, b = ResourceUtils.createBitmapFromSheet("icon_save", "iconRes"), a.addChild(b), a.useZoomOut(!0), a.addOnClick(this.share5, this), this._container.addChild(a), a = new egret.TextField, a.text = "\u6536\u85cf", a.size = 26, a.x = 462, a.y = 305, this._container.addChild(a));
            this._container.cacheAsBitmap = !0
        };
        b.prototype.share4 = function() {
            GameMainLayer.instance.showCodeAwardLayer()
        };
        b.prototype.share5 = function() {
            GameMainLayer.instance.showShareTipLayer("share_5")
        };
        b.prototype.saveHandler = function() {};
        b.prototype.statisticsHandler = function() {
            GameMainLayer.instance.showStatisticsLayer()
        };
        b.prototype.awardHandler = function() {
            GameMainLayer.instance.showCodeAwardLayer()
        };
        b.prototype.onMusicClickHandler = function() {
            SoundManager.getInstance().switchMusic();
            var a = SoundManager.getInstance().getMusicStatus();
            this._musicBtn.setButtonStatus(a);
            this._musicLabel.text = "\u97f3\u4e50:" + ["\u5173", "\u5f00"][a - 1]
        };
        b.prototype.onEffectClickHandler = function() {
            SoundManager.getInstance().switchEffect();
            var a = SoundManager.getInstance().getEffectStatus();
            this._effectBtn.setButtonStatus(a)
        };
        b.prototype.onClickHandler = function() {
            GameMainLayer.instance.hideSetLayer()
        };
        b.prototype.onMaskHandler = function() {};
        return b
    } (BaseContainer);
GameSetLayer.prototype.__class__ = "GameSetLayer";
var MusicButton = function(c) {
    function b() {
        c.call(this);
        this.anchorX = this.anchorY = 0.5
    }
    __extends(b, c);
    b.prototype.initButton = function(a, b, c) {
        this.open = ResourceUtils.createBitmapFromSheet(a, "commonRes");
        this.addChild(this.open);
        this.close = ResourceUtils.createBitmapFromSheet(b, "commonRes");
        this.addChild(this.close);
        this.close.x = 28;
        this.close.y = 8;
        this.close.visible = !1
    };
    b.prototype.setButtonStatus = function(a) {
        this.close.visible = 1 == a;
        this.open.visible = 1 != a
    };
    return b
} (SimpleButton);
MusicButton.prototype.__class__ = "MusicButton";
var MusicStatus; (function(c) {
    c[c.closed = 1] = "closed";
    c[c.open = 2] = "open"
})(MusicStatus || (MusicStatus = {}));
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    HPProgress = function(c) {
        function b(a, b) {
            void 0 === b && (b = "");
            c.call(this);
            this._isMask = !0;
            this._textureHeight = 0;
            "" != b && (this._bg = ResourceUtils.createBitmapFromSheet(b, "commonRes"), this.addChild(this._bg));
            var g = ResourceUtils.createBitmapFromSheet(a, "commonRes");
            this._barWidth = g.width;
            this._textureHeight = g.height;
            this._bar = g;
            this.addChild(this._bar)
        }
        __extends(b, c);
        Object.defineProperty(b.prototype, "percentage", {
            get: function() {
                return this._percentage
            },
            set: function(a) {
                this._percentage = a;
                this.setProgress(this._percentage, 100)
            },
            enumerable: !0,
            configurable: !0
        });
        Object.defineProperty(b.prototype, "isMask", {
            get: function() {
                return this._isMask
            },
            set: function(a) {
                this._isMask = a
            },
            enumerable: !0,
            configurable: !0
        });
        b.prototype.setOffset = function(a, b) {
            this._bar.x = a;
            this._bar.y = b
        };
        b.prototype.setProgress = function(a, b) {
            this._isMask && !this._bar.mask && (this._bar.mask = {
                x: 0,
                y: 0,
                width: this._barWidth,
                height: this._textureHeight
            });
            this._percentage = a / b * 100;
            this._isMask ? (this._bar.mask.width = this._barWidth * a / b, this._bar._setDirty()) : this._bar.scaleX = a / b
        };
        return b
    } (egret.DisplayObjectContainer);
HPProgress.prototype.__class__ = "HPProgress";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    LoadingUI = function(c) {
        function b() {
            c.call(this);
            this.createView()
        }
        __extends(b, c);
        b.prototype.createView = function() {
            addAction("loading_resize", this.resize, this);
            this._bg = ResourceUtils.createBitmapByName("loadingBg");
            this.addChild(this._bg);
            this.textField = new egret.TextField;
            this.addChild(this.textField);
            this.textField.y = GameUtils.getWinHeight() / 2;
            this.textField.width = GameUtils.getWinWidth();
            this.textField.height = 100;
            this.textField.textColor = 0;
            this.textField.textAlign = "center";
            if (PlatformFactory.getPlatform().name != PlatformTypes.WEIXIN) {
                this._btn = new SimpleButton;
                this.addChild(this._btn);
                this._btn.x = GameUtils.getWinWidth() / 2 + 20;
                this._btn.y = GameUtils.getWinHeight() - 120;
                this._btn.anchorX = this._btn.anchorY = 0.5;
                var a = ResourceUtils.createBitmapByName("loading_btn");
                this._btn.addChild(a);
                this._btn.useZoomOut(!0);
                this._btn.addOnClick(this.confirmHandler, this)
            } else RES.loadGroup("preload")
        };
        b.prototype.confirmHandler = function() {
            this._btn.visible = !1;
            RES.loadGroup("preload")
        };
        b.prototype.resize = function() {};
        b.prototype.setProgress = function(a, b) {
            this.textField.text = "\u6e38\u620f\u52a0\u8f7d\u4e2d..." + a + "/" + b
        };
        return b
    } (egret.Sprite);
LoadingUI.prototype.__class__ = "LoadingUI";
__extends = this.__extends ||
    function(c, b) {
        function a() {
            this.constructor = c
        }
        for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
        a.prototype = b.prototype;
        c.prototype = new a
    }; (function(c) {
    var b = function(a) {
        function b() {
            a.call(this);
            this._json = {};
            this._subValue = {}
        }
        __extends(b, a);
        b.prototype.analyzeData = function(b, d) {
            a.prototype.analyzeData.call(this, b, d);
            var c = this.fileDic[b.name],
                f;
            for (f in c) this.addSubkey(f, b.name),
                this._json[f] = b.name
        };
        b.prototype.getRes = function(a) {
            if (this.fileDic[a]) return this.fileDic[a];
            if (!this._subValue[a] && this._json[a]) {
                var b = this.fileDic[this._json[a]][a],
                    d = new c.ResourceItem("", "", ""),
                    f = egret.Injector.getInstance(c.AnalyzerBase, b.t);
                d.name = a;
                d.url = b.u || "";
                f.analyzeData(d, JSON.stringify(b.d));
                this._subValue[a] = f.getRes(a)
            }
            return this._subValue[a]
        };
        return b
    } (c.JsonAnalyzer);
    c.JsonSheetAnalyzer = b;
    b.prototype.__class__ = "RES.JsonSheetAnalyzer"
})(RES || (RES = {}));
var PaymentModel = function() {
    function c(b) {
        this._data = b
    }
    Object.defineProperty(c.prototype, "id", {
        get: function() {
            return this._data.id
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "mode", {
        get: function() {
            return this._data.mode
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "title", {
        get: function() {
            return this._data.title
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "description", {
        get: function() {
            return this._data.description
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "type", {
        get: function() {
            return this._data.type
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "need", {
        get: function() {
            return this._data.need
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "currency", {
        get: function() {
            return this._data.currency
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "icon", {
        get: function() {
            return this._data.icon
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "tip", {
        get: function() {
            return this._data.tip
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "goodType", {
        get: function() {
            return this._data.good
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "descColor", {
        get: function() {
            return this.mode == PaymentMode.USE ? 14664856 : 16777215
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "productId", {
        get: function() {
            return this._data.productId
        },
        enumerable: !0,
        configurable: !0
    });
    Object.defineProperty(c.prototype, "time", {
        get: function() {
            return DataCenter.getInstance().countdown.getTime("payment_" + this.type)
        },
        enumerable: !0,
        configurable: !0
    });
    c.prototype.countdown = function() {
        this._data.countdown && DataCenter.getInstance().countdown.setTime("payment_" + this.type, this._data.countdown)
    };
    c.prototype.getFormatDescription = function() {
        var b = this.description || "";
        if ("coin" == this.type) {
            var a = new NumberModel;
            a.add(DataCenter.getInstance().getBossHP());
            a.times(10);
            return b.replace("{0}", a.getNumer())
        }
        return b
    };
    c.prototype.getFormatValue = function() {
        var b = this.need;
        return "cny" == this.currency ? "\uffe5" + b: "jifen" == this.currency ? b + "\u79ef\u5206": 0 == b ? "\u514d\u8d39": b.toString()
    };
    return c
} ();
PaymentModel.prototype.__class__ = "PaymentModel";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    CoinElf = function(c) {
        function b(a) {
            c.call(this);
            this._itemsTouch = this._curElf = null;
            this._timeid = 0;
            this._clicked = !1;
            this._coin = a;
            this._itemsTouch = new ItemsTouchManager;
            this._itemsTouch.setController(new TouchController(this.click, this))
        }
        __extends(b, c);
        b.prototype.showElf = function() {
            var a = this;
            GameUtils.preloadAnimation("elf_json",
                function() {
                    a._curElf = new Movie("elf_json", "elf");
                    a._spr = new egret.DisplayObjectContainer;
                    var b = new egret.Sprite;
                    b.anchorX = a._spr.anchorY = 0.5;
                    b.graphics.beginFill(16711680);
                    b.graphics.drawRect(0, 0, 70, 70);
                    b.graphics.endFill();
                    b.width = 70;
                    b.alpha = 0;
                    b.height = 70;
                    b.y = -35;
                    b.x = 0;
                    a._spr.addChild(b);
                    a._spr.addChild(a._curElf);
                    a._curElf.play("1");
                    a.addChild(a._spr);
                    a._spr.y = -50;
                    a._spr.x = GameUtils.random(0, 600);
                    a._itemsTouch.addItems(b);
                    var c = 180 + GameUtils.random(0, 60);
                    TweenLite.to(a._spr, 1, {
                        x: 110 + GameUtils.random(0, 400),
                        y: c,
                        onComplete: function() {
                            a._timeid = Time.setTimeout(a.click, a, 4E3);
                            egret.Tween.get(a._spr, {
                                loop: !0
                            }).to({
                                    y: c + 15
                                },
                                1E3).to({
                                    y: c
                                },
                                1E3)
                        }
                    })
                })
        };
        b.prototype.click = function() {
            var a = this;
            console.log("CLICK");
            if (!this._clicked) {
                egret.Tween.removeTweens(this._spr);
                this._clicked = !0;
                this._itemsTouch.removeItems(this._curElf);
                this._curElf.play("2");
                DataCenter.getInstance().statistics.addValue(StatisticsType.gift);
                var b = ResourceUtils.createBitmapFromSheet("box_normal");
                this.addChild(b);
                b.x = this._spr.x;
                b.y = this._spr.y;
                b.anchorX = b.anchorY = 0.5;
                b.scaleX = b.scaleY = 0.8;
                egret.Tween.get(b).to({
                        y: 516
                    },
                    3E3).call(function() {
                        egret.Tween.removeTweens(b);
                        GameUtils.removeFromParent(b);
                        var c = ResourceUtils.createBitmapFromSheet("box_open");
                        c.x = b.x;
                        c.y = b.y;
                        c.anchorX = c.anchorY = 0.5;
                        c.scaleX = c.scaleY = 0.8;
                        a.addChild(c);
                        DataCenter.getInstance().statistics.addValue(StatisticsType.openbox);
                        var e = a.localToGlobal(b.x, b.y),
                            h = new NumberModel;
                        h.add(a._coin);
                        h = {
                            coins: h.pieces(2),
                            type: "1"
                        };
                        doAction(Const.DROP_COIN, h, e.x, e.y);
                        egret.Tween.get(c).wait(1E3).to({
                                alpha: 0
                            },
                            200).call(function() {
                                GameUtils.removeFromParent(c)
                            })
                    });
                egret.Tween.get(b, {
                    loop: !0
                }).to({
                        rotation: 10
                    },
                    250).to({
                        rotation: 0
                    },
                    250).to({
                        rotation: -10
                    },
                    250).to({
                        rotation: 0
                    },
                    250);
                this.moveBack()
            }
        };
        b.prototype.moveBack = function() {
            var a = this;
            Time.clearTimeout(this._timeid);
            TweenLite.to(this._spr, 1, {
                x: GameUtils.random(0, 600),
                y: -50,
                onComplete: function() {
                    GameUtils.removeFromParent(a._spr);
                    GameUtils.removeFromParent(a._curElf);
                    a._curElf = null;
                    a._spr = null;
                    a._clicked = !1
                }
            })
        };
        b.prototype.begin = function(a) {
            Time.setTimeout(this.showElf, this, a)
        };
        return b
    } (BaseContainer);
CoinElf.prototype.__class__ = "CoinElf";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    WXAwardBox = function(c) {
        function b() {
            c.call(this);
            addAction(Const.AWARD_BOX_TIP, this.awardTip, this)
        }
        __extends(b, c);
        b.prototype.awardTip = function() {
            this.onMenuShareAppMessage(ProxyUtils.awdTo);
            GameMainLayer.instance.showShareTipLayer("awardbox")
        };
        return b
    } (WXShareBase);
WXAwardBox.prototype.__class__ = "WXAwardBox";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    WXShareKillBox = function(c) {
        function b() {
            c.call(this);
            addAction(Const.BOSS_KILLED, this.killed, this)
        }
        __extends(b, c);
        b.prototype.killed = function(a, b) {
            var c = this;
            "0" != this.getShare().getValue("boxenemy") && b && (this.shareFriend({
                    t: "boxenemy"
                },
                function() {
                    c.getShare().removeValue("boxenemy")
                },
                this.removeMask), GameMainLayer.instance.showShareTipLayer("share_3"))
        };
        return b
    } (WXShareBase);
WXShareKillBox.prototype.__class__ = "WXShareKillBox";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    WXShareNextLevel = function(c) {
        function b() {
            c.call(this);
            addAction(Const.UNLOCK_LEVEL, this.unlockLevel, this)
        }
        __extends(b, c);
        b.prototype.unlockLevel = function(a) {
            var b = this;
            "0" != this.getShare().getValue("nextlevel") && (this.shareFriend({
                    t: "nextlevel"
                },
                function() {
                    b.getShare().removeValue("nextlevel");
                    var a = new SingleProxy({
                        mod: "User",
                        "do": "shareNextLevel"
                    });
                    a.addEventListener(ProxyEvent.RESPONSE_SUCCEED,
                        function(a) {
                            WXUtils.showAward(a.responseData.award)
                        },
                        b);
                    a.load()
                },
                this.removeMask), GameMainLayer.instance.showShareTipLayer("share_1"))
        };
        return b
    } (WXShareBase);
WXShareNextLevel.prototype.__class__ = "WXShareNextLevel";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    WXShareUnlockSkill = function(c) {
        function b() {
            c.call(this);
            addAction(Const.CLICK_CD_SKILL, this.clickCdSkill, this)
        }
        __extends(b, c);
        b.prototype.clickCdSkill = function(a) {
            var b = this;
            "0" != this.getShare().getValue("cdskill") && (this.shareFriend({
                    t: "cdskill",
                    skId: a
                },
                function() {
                    b.getShare().removeValue("cdskill")
                },
                this.removeMask), GameMainLayer.instance.showShareTipLayer("share_2"))
        };
        return b
    } (WXShareBase);
WXShareUnlockSkill.prototype.__class__ = "WXShareUnlockSkill";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    WxShareAll = function(c) {
        function b() {
            c.call(this);
            this._isQuery = !1;
            this._data = this._defaultShare = null;
            addAction(Const.CLOSE_SHARE_TIP, this.closeShareTip, this);
            addAction(Const.CHANGE_DEFAULT_SHARE, this.defaultShare, this)
        }
        __extends(b, c);
        b.prototype.shareRequest = function() {
            this._isQuery || this.share()
        };
        b.prototype.defaultShare = function(a) {
            this._defaultShare = a;
            this.onMenuShareAppMessage(this.getData());
            this.onMenuShareQQ(this.getData());
            this.onMenuShareWeibo(this.getData())
        };
        b.prototype.getData = function() {
            return null != this._defaultShare ? this._defaultShare: this._data
        };
        b.prototype.closeShareTip = function() {
            this.onMenuShareTimeline(this._data);
            this.onMenuShareAppMessage(this.getData())
        };
        b.prototype.confAPI = function(a) {
            var b = this;
            wx.ready(function() {
                wx.showMenuItems({
                    menuList: "menuItem:share:appMessage menuItem:share:timeline menuItem:profile menuItem:favorite menuItem:share:qq menuItem:share:weiboApp".split(" ")
                });
                b.onMenuShareTimeline(a);
                b.onMenuShareAppMessage(a);
                b.onMenuShareQQ(a);
                b.onMenuShareWeibo(a)
            })
        };
        b.prototype.share = function() {
            var a = this;
            this.loadShareInfo({
                    t: "shareall"
                },
                function(b) {
                    a._data = b;
                    a.confAPI(b)
                },
                this, !0)
        };
        b.prototype.getQueryData = function() {
            var a = decodeURIComponent(GameUtils.getQuery("title", !0)),
                b = decodeURIComponent(GameUtils.getQuery("desc", !0)),
                c = decodeURIComponent(GameUtils.getQuery("link", !0)),
                e = decodeURIComponent(GameUtils.getQuery("imgUrl", !0)),
                h = decodeURIComponent(GameUtils.getQuery("timestamp", !0)),
                f = decodeURIComponent(GameUtils.getQuery("nonceStr", !0)),
                k = decodeURIComponent(GameUtils.getQuery("signature", !0));
            if (null != a && null != b && null != c && null != e && null != h && null != f && null != k) {
                var l = {};
                l.timestamp = parseInt(h);
                l.nonceStr = f;
                l.signature = k;
                l.title = a;
                l.desc = b;
                l.link = c;
                l.imgUrl = e;
                return l
            }
            return null
        };
        b.prototype.shareQuery = function() {
            var a = this.getQueryData();
            if (null != a) {
                this._data = a;
                this._isQuery = !0;
                var b = new BodyConfig;
                b.appId = "wxa725d9c20ec312ae";
                b.nonceStr = a.nonceStr;
                b.signature = a.signature;
                b.timestamp = a.timestamp;
                b.jsApiList = "onMenuShareTimeline onMenuShareAppMessage onMenuShareQQ onMenuShareWeibo showMenuItems chooseWXPay".split(" ");
                wx.config(b);
                this.confAPI(a)
            }
        };
        return b
    } (WXShareBase);
WxShareAll.prototype.__class__ = "WxShareAll";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    Mask = function(c) {
        function b(a) {
            c.call(this);
            this.texture = a;
            this.tempHeight = 0;
            this.width = this.height = 100;
            this.touchEnabled = !0;
            this.touchChildren = !1;
            this.alpha = 0
        }
        __extends(b, c);
        b.prototype.show = function(a, b, c, e) {
            var h = this.width,
                f = this.height;
            a = parseInt(a.toString());
            b = parseInt(b.toString());
            c = parseInt(c.toString());
            e = parseInt(e.toString());
            a + c > h && (a = h - c);
            b + e > f && (b = f - e);
            0 > a && (a = 0);
            0 > b && (b = 0); (0 > a || 0 > b || a + c > h || b + e > f) && egret.Logger.fatal("Mask::\u53c2\u6570\u9519\u8bef");
            this.bitmap1 || (this.bitmap1 = new egret.Bitmap(this.texture), this.addChild(this.bitmap1), this.bitmap2 = new egret.Bitmap(this.texture), this.addChild(this.bitmap2), this.bitmap3 = new egret.Bitmap(this.texture), this.addChild(this.bitmap3), this.bitmap4 = new egret.Bitmap(this.texture), this.addChild(this.bitmap4), this.bitmap5 = new egret.Bitmap(this.texture), this.addChild(this.bitmap5), this.bitmap6 = new egret.Bitmap(this.texture), this.addChild(this.bitmap6), this.bitmap7 = new egret.Bitmap(this.texture), this.addChild(this.bitmap7), this.bitmap8 = new egret.Bitmap(this.texture), this.addChild(this.bitmap8));
            this.bitmap1.width = a;
            this.bitmap1.height = b - this.tempHeight;
            this.bitmap1.y = this.tempHeight;
            this.bitmap2.x = a;
            this.bitmap2.width = c;
            this.bitmap2.height = b - this.tempHeight;
            this.bitmap2.y = this.tempHeight;
            this.bitmap3.x = a + c;
            this.bitmap3.width = h - a - c;
            this.bitmap3.height = b - this.tempHeight;
            this.bitmap3.y = this.tempHeight;
            this.bitmap4.y = b;
            this.bitmap4.width = a;
            this.bitmap4.height = e;
            this.bitmap5.x = a + c;
            this.bitmap5.y = b;
            this.bitmap5.width = h - a - c;
            this.bitmap5.height = e;
            this.bitmap6.y = b + e;
            this.bitmap6.width = a;
            this.bitmap6.height = f - b - e;
            this.bitmap7.x = a;
            this.bitmap7.y = b + e;
            this.bitmap7.width = c;
            this.bitmap7.height = f - b - e;
            this.bitmap8.x = a + c;
            this.bitmap8.y = b + e;
            this.bitmap8.width = h - a - c;
            this.bitmap8.height = f - b - e
        };
        b.prototype.changeTexture = function(a) {
            this.texture = a;
            this.bitmap1 && (this.bitmap1.texture = a, this.bitmap2.texture = a, this.bitmap3.texture = a, this.bitmap4.texture = a, this.bitmap5.texture = a, this.bitmap6.texture = a, this.bitmap7.texture = a, this.bitmap8.texture = a)
        };
        return b
    } (egret.DisplayObjectContainer);
Mask.prototype.__class__ = "Mask";
var SoundManager = function() {
    function c() {
        this._effect = new SoundEffects;
        this._back = new SoundBg;
        var b = egret.localStorage.getItem("bearjmoerMusic") || "2",
            a = egret.localStorage.getItem("bjmoerSound") || "2";
        this._musicStop = parseInt(b);
        this._soundStop = parseInt(a)
    }
    c.getInstance = function() {
        return null == this._instance ? this._instance = new c: this._instance
    };
    c.prototype.getMusicStatus = function() {
        return this._musicStop
    };
    c.prototype.getEffectStatus = function() {
        return this._soundStop
    };
    c.prototype.isMusicOpen = function() {
        return 1 != this._musicStop
    };
    c.prototype.isSoundOpen = function() {
        return 1 != this._soundStop
    };
    c.prototype.playBackgroundMusic = function() {
        this.isMusicOpen() && this._back.play("bgSound")
    };
    c.prototype.stopBackgroundMusic = function() {
        this._back.stop()
    };
    c.prototype.resumeBackMusic = function() {
        this.isMusicOpen() && this._back.play("bgSound")
    };
    c.prototype.pauseBackMusic = function() {
        this.isMusicOpen() && this._back.stop()
    };
    c.prototype.playEffect = function(b) {};
    c.prototype.stopEffect = function(b) {
        this.isSoundOpen() && this._effect.stop(b)
    };
    c.prototype.switchMusic = function() {
        this._musicStop += 1;
        2 < this._musicStop && (this._musicStop = 1);
        1 == this._musicStop ? this.stopBackgroundMusic() : this._back.play("bgSound");
        this.setUserInfo()
    };
    c.prototype.switchEffect = function() {
        this._soundStop += 1;
        2 < this._soundStop && (this._soundStop = 1);
        this.setUserInfo()
    };
    c.prototype.setUserInfo = function() {
        egret.localStorage.setItem("bearjmoerMusic", this._musicStop.toString());
        egret.localStorage.setItem("bjmoerSound", this._soundStop.toString())
    };
    return c
} ();
SoundManager.prototype.__class__ = "SoundManager";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    ShakeAnimation = function(c) {
        function b(a, b, g, e) {
            void 0 === e && (e = -1);
            c.call(this, a);
            this._count = this._strengthY = this._strengthX = 0;
            this._count = e;
            this._strengthX = b;
            this._strengthY = g
        }
        __extends(b, c);
        b.prototype.update = function(a) {
            var d = b._original_position[this._target.hashCode],
                c = GameUtils.random( - this._strengthX, this._strengthX),
                e = GameUtils.random( - this._strengthY, this._strengthY);
            1 == a ? (this._target.x = d.pt.x, this._target.y = d.pt.y, a = d.ref.indexOf(this), -1 < a && d.ref.splice(a, 1), 0 == d.ref.length && delete b._original_position[this._target.hashCode]) : (this._target.x = d.pt.x + c, this._target.y = d.pt.y + e)
        };
        b.prototype.run = function(a) {
            var d = b._original_position;
            d.hasOwnProperty(a.hashCode) || (d[a.hashCode] = {},
                d[a.hashCode].ref = [], d[a.hashCode].pt = new egret.Point(a.x, a.y));
            d[a.hashCode].ref.push(this);
            c.prototype._run.call(this, a)
        };
        b._original_position = {};
        return b
    } (GameAnimation);
ShakeAnimation.prototype.__class__ = "ShakeAnimation";
var DisplayTouchHandler = function() {
    function c() {}
    c.prototype.dispose = function() {
        this._display.removeEventListener(egret.TouchEvent.TOUCH_BEGIN, this.onTouchBeginHandler, this);
        this._display.removeEventListener(egret.Event.REMOVED, this.dispose, this)
    };
    c.prototype.init = function(b, a, d) {
        this._display = b;
        this._selector = a;
        this._target = d;
        this._display.touchEnabled = !0;
        this._display.addEventListener(egret.TouchEvent.TOUCH_BEGIN, this.onTouchBeginHandler, this);
        this._display.addEventListener(egret.Event.REMOVED, this.dispose, this)
    };
    c.prototype.onTouchBeginHandler = function(b) {
        this._display && this._display.visible && (this._display.removeEventListener(egret.TouchEvent.TOUCH_BEGIN, this.onTouchBeginHandler, this), this._display.addEventListener(egret.TouchEvent.TOUCH_END, this.onTouchEndHandler, this))
    };
    c.prototype.onTouchEndHandler = function(b) {
        this._display.addEventListener(egret.TouchEvent.TOUCH_BEGIN, this.onTouchBeginHandler, this);
        this._display.removeEventListener(egret.TouchEvent.TOUCH_END, this.onTouchEndHandler, this);
        this._display && this._display.visible && this._selector && this._target && this._selector.call(this._target)
    };
    c.create = function(b, a, d) {
        void 0 === a && (a = null);
        void 0 === d && (d = null);
        var g = new c;
        g.init(b, a, d);
        return g
    };
    return c
} ();
DisplayTouchHandler.prototype.__class__ = "DisplayTouchHandler";
var ProxyListener = function() {
    function c() {
        this.initListeners()
    }
    c.prototype.initListeners = function() {
        addAction("proxy_response_succeed", this.responseChangeAction, this);
        addAction("proxy_response_succeed", this.responseCacheFilter, this)
    };
    c.prototype.responseChangeAction = function(b) {
        b.responseData.hasOwnProperty("c") && ProxyUpdate.update(b, ProxyCache.getCache())
    };
    c.prototype.responseCacheFilter = function(b) {
        ProxyCache.setCache(b)
    };
    return c
} ();
ProxyListener.prototype.__class__ = "ProxyListener";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    BottomView = function(c) {
        function b() {
            c.call(this);
            this._tabTips = {};
            this.initButton();
            this.init()
        }
        __extends(b, c);
        b.prototype.initButton = function() {
            this._button1 = new TabButton;
            this._button1.initButton("tab_1_1", "tab_1_2");
            this.addChild(this._button1);
            this._button1.name = "mainButton";
            this._button1.x = 75;
            this._button2 = new TabButton;
            this._button2.initButton("tab_2_1", "tab_2_2");
            this.addChild(this._button2);
            this._button2.x = 238;
            this._button2.name = "heroButton";
            this._button3 = new TabButton;
            this._button3.initButton("tab_3_1", "tab_3_2");
            this.addChild(this._button3);
            this._button3.x = 401;
            this._button3.name = "holyButton";
            this._button4 = new TabButton;
            this._button4.initButton("tab_4_1", "tab_4_2");
            this.addChild(this._button4);
            this._button4.x = 565;
            this._button4.name = "adButton";
            addAction(Const.CHANGE_COIN, this.onChangeCoin, this);
            this.onChangeCoin()
        };
        b.prototype.hideTab = function() {
            this._button3.visible = !1;
            this._button4.visible = !1
        };
        b.prototype.showTab = function() {
            this._button3.visible = !0;
            this._button4.visible = !0
        };
        b.prototype.refreshTab = function(a, b, c, e) {
            b ? this._tabTips[a] || (b = ResourceUtils.createBitmapFromSheet("new_hero_tip"), b.x = c, b.y = e, b.anchorX = b.anchorY = 0.5, this.addChild(b), this._tabTips[a] = b, egret.Tween.get(b, {
                loop: !0
            }).to({
                    scaleX: 1.2,
                    scaleY: 1.2
                },
                500).to({
                    scaleX: 1,
                    scaleY: 1
                },
                500)) : this._tabTips[a] && (egret.Tween.removeTweens(this._tabTips[a]), this.removeChild(this._tabTips[a]), delete this._tabTips[a])
        };
        b.prototype.onChangeCoin = function() {
            this.refreshTab("hero", DataCenter.getInstance().hasEnoughCallHero(), 190, -15);
            this.refreshTab("skill", DataCenter.getInstance().hasEnoughNewSkill(), 30, -15)
        };
        return b
    } (TabView);
BottomView.prototype.__class__ = "BottomView";
var TabButton = function(c) {
    function b() {
        c.call(this);
        this.anchorX = this.anchorY = 0.5
    }
    __extends(b, c);
    b.prototype.initButton = function(a, b) {
        var c = ResourceUtils.createBitmapFromSheet(a, "commonRes");
        c.anchorX = c.anchorY = 0.5;
        c.x = 75;
        c.y = 24;
        this.addChild(c);
        c = ResourceUtils.createBitmapFromSheet(b, "commonRes");
        c.anchorX = c.anchorY = 0.5;
        this.addChild(c);
        c.x = 95;
        c.y = 24;
        c.visible = !1
    };
    b.prototype.setFrameChild = function(a) {
        this.getChildAt(1).visible = 1 < a
    };
    return b
} (SimpleButton);
TabButton.prototype.__class__ = "TabButton";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    HeroListLayer = function(c) {
        function b() {
            c.call(this);
            this._dataList = [];
            this.init()
        }
        __extends(b, c);
        b.prototype.init = function() {
            this._upContainer = new egret.DisplayObjectContainer;
            this.addChild(this._upContainer);
            this._upContainer.y = -134;
            this._dpsLabel = new egret.TextField;
            this._dpsLabel.size = 24;
            this._dpsLabel.x = 120;
            this._dpsLabel.y = 5;
            this._dpsLabel.textColor = 16639912;
            this._dpsLabel.anchorX = 0;
            this._upContainer.addChild(this._dpsLabel);
            var a = new egret.TextField;
            a.size = 24;
            a.x = 12;
            a.y = 5;
            this._upContainer.addChild(a);
            a.text = "DPS";
            a.textColor = 16639912;
            this._upContainer.cacheAsBitmap = !0;
            this._dataList = [];
            this.coinChange();
            this._heroTableView = new TableView;
            this._heroTableView.x = 13;
            this._heroTableView.y = -88;
            this.addChild(this._heroTableView);
            this._heroTableView.width = GameUtils.getWinWidth();
            this._heroTableView.height = 300;
            this._heroTableView.setList(this._dataList, Direction.VERTICAL, this, GameUtils.getWinWidth(), 102);
            addAction(Const.HERO_LIST_CHANGE, this.dataChange, this);
            addAction(Const.CHANGE_COIN, this.coinChange, this);
            addAction(Const.REFRESH_UNOPEN_LIST, this.refreshUnCallHero, this);
            addAction(Const.RESET_GAME, this.resetList, this);
            addAction(Const.REFRESH_SKILL_VIEW, this.refreshDPS, this);
            addAction(Const.REFRESH_BOSS, this.refreshDPS, this, 4);
            this.refreshDPS()
        };
        b.prototype.resetList = function() {
            this._dataList = [];
            this.coinChange();
            this._heroTableView && this._heroTableView.reloadData(this._dataList)
        };
        b.prototype.refreshDPS = function() {
            applyFilters(Const.ISBOSS, !1) ? this._dpsLabel.text = HeroController.getInstance().currentBossDPS: this._dpsLabel.text = HeroController.getInstance().currentDPS
        };
        b.prototype.coinChange = function() {
            for (var a = DataCenter.getInstance().totalData.heroList, b = RES.getRes("heroConfig"), c = 0; c < Const.maxHero; c++) {
                var e = 1001 + c,
                    e = GameUtils.getCallCost(b[e].callCost);
                if (!1 == DataCenter.getInstance().hasEnoughSc(e)) break
            }
            c += 1;
            c > Const.maxHero && (c = Const.maxHero);
            for (e = b = 1; e <= Const.maxHero; e++) a[1E3 + e] && (b = e);
            b < c && (b = c);
            b < DataCenter.getInstance().totalData.showHeroNum ? b = DataCenter.getInstance().totalData.showHeroNum: DataCenter.getInstance().totalData.showHeroNum = b;
            if (b != this._dataList.length) {
                this._dataList = [];
                for (c = 1; c <= b; c++) {
                    var e = 1E3 + c,
                        h = HeroModel.createByConfigId(e);
                    h.refreshDataWithSkill();
                    this._dataList.push(h);
                    a[e] && (h.lv = a[e].lv)
                }
                this._heroTableView && this._heroTableView.reloadData(this._dataList)
            }
        };
        b.prototype.refreshUnCallHero = function() {
            for (var a = 0; a < this._dataList.length; a++) this._dataList[a].refreshDataWithSkill();
            this._heroTableView && this._heroTableView.reloadData(this._dataList)
        };
        b.prototype.dataChange = function(a, b) {
            this._dataList[b] = a;
            this._heroTableView.changeDataListOnly(this._dataList)
        };
        b.prototype.createItemRenderer = function() {
            return new HeroItemView
        };
        b.prototype.updateItemRenderer = function(a, b, c) {
            a.refreshView(b, c)
        };
        b.prototype.dispose = function() {
            c.prototype.dispose.call(this);
            removeEventsByTarget(this)
        };
        return b
    } (BaseContainer);
HeroListLayer.prototype.__class__ = "HeroListLayer";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    PaymentListLayer = function(c) {
        function b() {
            c.call(this);
            this.init()
        }
        __extends(b, c);
        b.prototype.init = function() {
            this._upContainer = new egret.DisplayObjectContainer;
            this.addChild(this._upContainer);
            this._upContainer.y = -133;
            this._allDamageLabel = new egret.TextField;
            this._allDamageLabel.size = 20;
            this._allDamageLabel.x = 90;
            this._allDamageLabel.anchorX = 1;
            this._upContainer.addChild(this._allDamageLabel);
            var a = ResourceUtils.createBitmapFromSheet("gold_icon", "commonRes");
            a.scaleX = a.scaleY = 0.68;
            a.x = 405;
            a.y = 5;
            this._upContainer.addChild(a);
            this._goldLabel = new egret.TextField;
            this._goldLabel.size = 24;
            this._goldLabel.x = 445;
            this._goldLabel.y = 5;
            this._goldLabel.text = "0";
            this._goldLabel.anchorX = 0;
            this._goldLabel.textColor = 16639912;
            this._upContainer.addChild(this._goldLabel);
            ProxyUtils.platformInfo == platformType_wanba && (this._jifenLabel = new egret.TextField, this._jifenLabel.size = 24, this._jifenLabel.x = 50, this._jifenLabel.y = 5, this._jifenLabel.textColor = 16639912, this.changeJifen(), this._jifenLabel.anchorX = 0, this._upContainer.addChild(this._jifenLabel));
            this._upContainer.cacheAsBitmap = !0;
            this.initTableView();
            this.changeGc();
            addAction(Const.CHANGE_DIAMOND, this.changeGc, this);
            addAction(Const.CHANGE_JIFEN, this.changeJifen, this)
        };
        b.prototype.changeJifen = function() {
            this._jifenLabel.text = "\u79ef\u5206:" + ProxyUtils.jifen
        };
        b.prototype.changeGc = function() {
            this._goldLabel.text = DataCenter.getInstance().getGcNum()
        };
        b.prototype.initTableView = function() {
            this._paymentTableView = new TableView;
            this._paymentTableView.x = 13;
            this._paymentTableView.y = -88;
            this.addChild(this._paymentTableView);
            this._paymentTableView.width = GameUtils.getWinWidth();
            this._paymentTableView.height = 300;
            this._paymentTableView.setList(DataCenter.getInstance().paymentList, Direction.VERTICAL, this, GameUtils.getWinWidth(), 102)
        };
        b.prototype.createItemRenderer = function() {
            return new PaymentItemView
        };
        b.prototype.updateItemRenderer = function(a, b, c) {
            a.refreshView(b, c)
        };
        return b
    } (BaseContainer);
PaymentListLayer.prototype.__class__ = "PaymentListLayer";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    RebornListLayer = function(c) {
        function b() {
            c.call(this);
            this._dataList = [];
            this.init()
        }
        __extends(b, c);
        b.prototype.init = function() {
            this._upContainer = new egret.DisplayObjectContainer;
            this.addChild(this._upContainer);
            this._upContainer.y = -134;
            this._allDamageLabel = new egret.TextField;
            this._allDamageLabel.size = 24;
            this._allDamageLabel.x = 120;
            this._allDamageLabel.y = 5;
            this._allDamageLabel.y = 5;
            this._allDamageLabel.textColor = 16639912;
            this._upContainer.addChild(this._allDamageLabel);
            var a = new egret.TextField;
            a.size = 24;
            a.x = 12;
            a.y = 5;
            this._upContainer.addChild(a);
            a.text = "\u6240\u6709\u4f24\u5bb3";
            a.textColor = 16639912;
            a = ResourceUtils.createBitmapFromSheet("holy_icon", "commonRes");
            a.x = 405;
            a.y = 3;
            this._upContainer.addChild(a);
            this._holyLabel = new egret.TextField;
            this._holyLabel.size = 24;
            this._holyLabel.x = 450;
            this._holyLabel.y = 5;
            this._holyLabel.textColor = 16639912;
            this._upContainer.addChild(this._holyLabel);
            this._upContainer.cacheAsBitmap = !0;
            this.refreshHolyNum();
            addAction(Const.CHANGE_HOLY_NUM, this.refreshHolyNum, this);
            addAction(Const.CHANGE_REBORN_SKILL, this.refreshTableView, this);
            this.initTableView();
            this.refreshAllDamage();
            addAction(Const.REBORN_SKILL_REFRESH, this.refreshAllDamage, this)
        };
        b.prototype.refreshAllDamage = function() {
            var a = 100 * SkillController.getInstance().holyAdd;
            this._allDamageLabel.text = 10 < a.toString().length ? "+" + a.toFixed(2) + "%": "+" + a + "%"
        };
        b.prototype.initDataList = function() {
            this._dataList = [];
            this._dataList.push({
                name: "\u5723\u7269",
                des: "\u8d2d\u4e70\u5723\u7269\u53ef\u83b7\u5f97\u5c5e\u6027\u52a0\u6210"
            });
            for (var a = DataCenter.getInstance().totalData.rebornSkillList, b = 0; b < a.length; b++) {
                var c = RebornSkillModel.createByConfigId(a[b].id);
                c.lv = a[b].lv;
                this._dataList.push(c)
            }
        };
        b.prototype.initTableView = function() {
            this.initDataList();
            this._rebornTableView = new TableView;
            this._rebornTableView.x = 13;
            this._rebornTableView.y = -88;
            this.addChild(this._rebornTableView);
            this._rebornTableView.width = GameUtils.getWinWidth();
            this._rebornTableView.height = 300;
            this._rebornTableView.setList(this._dataList, Direction.VERTICAL, this, GameUtils.getWinWidth(), 102)
        };
        b.prototype.refreshTableView = function() {
            this.initDataList();
            this._rebornTableView && this._rebornTableView.reloadData(this._dataList)
        };
        b.prototype.refreshHolyNum = function() {
            this._holyLabel.text = DataCenter.getInstance().totalData.holyNum.toString()
        };
        b.prototype.createItemRenderer = function() {
            return new RebornItemView
        };
        b.prototype.updateItemRenderer = function(a, b, c) {
            a.refreshView(b, c)
        };
        return b
    } (BaseContainer);
RebornListLayer.prototype.__class__ = "RebornListLayer";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    SkillDetailItemView = function(c) {
        function b() {
            c.call(this);
            this._isMain = !1;
            this.initView()
        }
        __extends(b, c);
        b.prototype.initView = function() {
            this._iconContainer = new egret.DisplayObjectContainer;
            this.addChild(this._iconContainer);
            this._nameLabel = new egret.TextField;
            this.addChild(this._nameLabel);
            this._nameLabel.size = 20;
            this._nameLabel.x = 85;
            this._nameLabel.y = 0;
            this._desLabel = new egret.TextField;
            this.addChild(this._desLabel);
            this._desLabel.size = 20;
            this._desLabel.x = 85;
            this._desLabel.y = 25;
            this._timeLabel = new egret.TextField;
            this._timeLabel.size = 20;
            this._timeLabel.x = 85;
            this._timeLabel.y = 50;
            this.addChild(this._timeLabel);
            this.notOpenShow()
        };
        b.prototype.notOpenShow = function() {
            this._lockBg = ResourceUtils.createBitmapFromSheet("head_mask", "commonRes");
            this._lockBg.width = this._lockBg.height = 70;
            this._lockBg.alpha = 0.7;
            this.addChild(this._lockBg);
            this._unlockLabel = new egret.TextField;
            this._unlockLabel.text = "\u89e3\u9501";
            this._unlockLabel.size = 20;
            this.addChild(this._unlockLabel);
            this._unlockLabel.x = 18;
            this._unlockLabel.y = 15;
            this._lvTitleLabel = new egret.TextField;
            this._lvTitleLabel.text = "Lv.";
            this._lvTitleLabel.size = 20;
            this.addChild(this._lvTitleLabel);
            this._lvTitleLabel.x = 5;
            this._lvTitleLabel.y = 35;
            this._lvLabel = new egret.TextField;
            this._lvLabel.size = 20;
            this._lvLabel.textColor = 5292278;
            this.addChild(this._lvLabel);
            this._lvLabel.x = 33;
            this._lvLabel.y = 35
        };
        b.prototype.refreshView = function(a, b, c, e, h, f, k) {
            this._skillId = a;
            this._config = RES.getRes("skillConfig")[a];
            this._isOpen = b;
            this._lv = c;
            this._isMain = e;
            this._hero = f;
            this._iconContainer.removeChildren();
            a = ResourceUtils.createBitmapFromSheet(k, "iconRes");
            this._iconContainer.addChild(a);
            this.updateSkillInfo()
        };
        b.prototype.updateSkillInfo = function() {
            this._nameLabel.text = this._config.name || "\u6211\u662f\u6280\u80fd\u540d\u5b57";
            this._isOpen ? (this._lockBg.visible = !1, this._unlockLabel.visible = !1, this._lvTitleLabel.visible = !1, this._lvLabel.visible = !1) : (this._hero.isEvolution() ? this._lvLabel.text = this._config.level + 1E3: this._lvLabel.text = this._config.level, this._lockBg.visible = !0, this._unlockLabel.visible = !0, this._lvTitleLabel.visible = !0, this._lvLabel.visible = !0, this._lvTitleLabel.x = 5 + (this._iconContainer.width - (this._lvTitleLabel.width + this._lvLabel.width + 10)) / 2, this._lvLabel.x = this._lvTitleLabel.x + this._lvTitleLabel.width + 3);
            if (this._isMain) {
                var a;
                a = 0 < this._lv ? this._config.base + this._lv * this._config.value: this._config.base + 1 * this._config.value;
                a *= this._config.showTimes;
                10 < a.toString().length && (a = a.toFixed(2));
                this._desLabel.text = this._config.des.replace("{0}", a);
                this._timeLabel.visible = !0;
                this._timeLabel.text = "\u6301\u7eed\u65f6\u95f4\uff1a" + this._config.last.toString() + "\u79d2 | \u51b7\u5374\u65f6\u95f4\uff1a" + this._config.cd.toString() + "\u79d2"
            } else this._desLabel.text = this._config.des,
                this._timeLabel.visible = !1
        };
        b.prototype.dispose = function() {
            c.prototype.dispose.call(this)
        };
        return b
    } (BaseContainer);
SkillDetailItemView.prototype.__class__ = "SkillDetailItemView";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    SkillLayer = function(c) {
        function b() {
            c.call(this);
            this.initView()
        }
        __extends(b, c);
        b.prototype.initView = function() {
            var a = ResourceUtils.createBitmapFromSheet("skilllayer_mask1");
            this.addChild(a);
            a.y = -150;
            a.scaleY = 1.2;
            a = ResourceUtils.createBitmapFromSheet("skilllayer_mask2");
            this.addChild(a);
            a.y = -25;
            a.x = 30;
            this._labelContainer = new egret.DisplayObjectContainer;
            this.addChild(this._labelContainer);
            a = new egret.TextField;
            a.text = "\u5f53\u524dDPS";
            a.x = 95;
            a.y = 20;
            this._labelContainer.addChild(a);
            this._currentDPS = new egret.BitmapText;
            a = RES.getRes("battle_num_fnt_fnt");
            this._currentDPS.font = a;
            this._labelContainer.addChild(this._currentDPS);
            this._currentDPS.text = "1";
            this._currentDPS.x = 50;
            this._currentDPS.y = -18;
            a = new egret.TextField;
            this._labelContainer.addChild(a);
            a.text = "\u4f63\u5175DPS";
            a.x = 470;
            a.y = 25;
            this._heroDPS = new egret.BitmapText;
            a = RES.getRes("battle_num_fnt_fnt");
            this._heroDPS.font = a;
            this._labelContainer.addChild(this._heroDPS);
            this._heroDPS.anchorX = 0;
            this._heroDPS.x = 270;
            this._heroDPS.y = 25;
            a = new egret.TextField;
            this._labelContainer.addChild(a);
            a.text = "\u70b9\u51fb\u4f24\u5bb3";
            a.x = 470;
            a.y = -20;
            this._tapDamage = new egret.BitmapText;
            a = RES.getRes("battle_num_fnt_fnt");
            this._tapDamage.font = a;
            this._labelContainer.addChild(this._tapDamage);
            this._tapDamage.anchorX = 0;
            this._tapDamage.x = 300;
            this._tapDamage.y = -18;
            a = ResourceUtils.createBitmapFromSheet("skill_line_1", "commonRes");
            a.x = 30;
            a.y = 56;
            this._labelContainer.addChild(a);
            a = ResourceUtils.createBitmapFromSheet("skill_line_2", "commonRes");
            a.x = 240;
            a.y = 55;
            this._labelContainer.addChild(a);
            a = ResourceUtils.createBitmapFromSheet("skill_line_3", "commonRes");
            a.x = 272;
            a.y = 15;
            this._labelContainer.addChild(a);
            this._labelContainer.cacheAsBitmap = !0;
            this._skillListContainer = new egret.DisplayObjectContainer;
            this._skillListContainer.y = 100;
            this.addChild(this._skillListContainer);
            this.initSkillIcon();
            this.refreshView();
            addAction(Const.REFRESH_SKILL_VIEW, this.refreshView, this);
            addAction(Const.ENTER_FIGHT, this.refreshView, this, 2);
            addAction(Const.UNLOCK_MAIN_SKILL, this.refreshMainSkill, this);
            addAction(Const.REFRESH_CURRENT_DPS, this.refreshCurrentDPS, this);
            addFilter(Const.SKILL_LAYER_EXSIT, this.layerVisible, this);
            addAction(Const.RESET_GAME, this.resetGame, this)
        };
        b.prototype.layerVisible = function() {
            return this.visible
        };
        b.prototype.refreshCurrentDPS = function(a) {
            var b = this._heroDPS.text,
                c = new NumberModel;
            c.add(b);
            c.compare(a) ? this._currentDPS.text = b: this._currentDPS.text = a
        };
        b.prototype.refreshView = function() {
            applyFilters(Const.ISBOSS, !1) ? (this._tapDamage.text = HeroController.getInstance().mainHero.bossDps, this._heroDPS.text = HeroController.getInstance().currentBossDPS) : (this._tapDamage.text = HeroController.getInstance().mainHero.tapDamage, this._heroDPS.text = HeroController.getInstance().currentDPS)
        };
        b.prototype.initSkillIcon = function() {
            this._skillListContainer.removeChildren();
            for (var a = HeroController.getInstance().mainHero.skillList, b = 0; b < a.length; b++) {
                var c = new SkillIcon;
                c.initView(a[b], b + 1);
                this._skillListContainer.addChild(c);
                c.x = 20 + 100 * b
            }
        };
        b.prototype.refreshMainSkill = function() {
            for (var a = HeroController.getInstance().mainHero.skillList, b = 0; b < a.length; b++) this._skillListContainer.getChildAt(b).refreshView(a[b])
        };
        b.prototype.resetGame = function() {
            for (var a = HeroController.getInstance().mainHero.skillList, b = 0; b < a.length; b++) this._skillListContainer.getChildAt(b).resetIcon()
        };
        b.prototype.dispose = function() {
            c.prototype.dispose.call(this);
            removeEventsByTarget(this)
        };
        return b
    } (BaseContainer);
SkillLayer.prototype.__class__ = "SkillLayer";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    SkillListLayer = function(c) {
        function b() {
            c.call(this);
            this._dataList = [];
            this.init()
        }
        __extends(b, c);
        b.prototype.init = function() {
            this._upContainer = new egret.DisplayObjectContainer;
            this.addChild(this._upContainer);
            this._upContainer.y = -134;
            this._tapDamageLabel = new egret.TextField;
            this._tapDamageLabel.size = 24;
            this._tapDamageLabel.x = 120;
            this._tapDamageLabel.y = 5;
            this._tapDamageLabel.anchorX = 0;
            this._tapDamageLabel.textColor = 16639912;
            this._upContainer.addChild(this._tapDamageLabel);
            var a = new egret.TextField;
            a.size = 24;
            a.x = 12;
            a.y = 5;
            this._upContainer.addChild(a);
            a.text = "\u70b9\u51fb\u4f24\u5bb3";
            a.textColor = 16639912;
            this._upContainer.cacheAsBitmap = !0;
            this._dataList = [];
            var a = HeroController.getInstance().mainHero,
                b = {};
            b.lv = a.lv;
            b.name = "\u4e50\u4e50\u4fa0";
            b.des = "\u70b9\u51fb\u4f24\u5bb3:{0}";
            this._dataList.push(b);
            a = a.skillList;
            for (b = 0; b < a.length; b++) this._dataList.push(a[b]);
            this._dataList.push({
                name: "\u91cd\u751f",
                des: "\u5f00\u542f\u65b0\u751f,\u5404\u79cd\u5c5e\u6027\u52a0\u6210"
            });
            this._skillTableView = new TableView;
            this._skillTableView.x = 13;
            this._skillTableView.y = -88;
            this.addChild(this._skillTableView);
            this._skillTableView.width = GameUtils.getWinWidth();
            this._skillTableView.height = 300;
            this._skillTableView.setList(this._dataList, Direction.VERTICAL, this, GameUtils.getWinWidth(), 102);
            addAction(Const.MAIN_HERO_SKILL_CHANGE, this.dataChange, this);
            addAction(Const.REFRESH_UNOPEN_LIST, this.refreshView, this);
            addAction(Const.RESET_GAME, this.refreshView, this);
            addAction(Const.REFRESH_SKILL_VIEW, this.refreshTapDamage, this);
            addAction(Const.REFRESH_BOSS, this.refreshTapDamage, this, 3);
            addAction(Const.CHANGE_COIN, this.refreshCoin, this);
            this.refreshTapDamage();
            this.refreshCoin()
        };
        b.prototype.refreshCoin = function() {};
        b.prototype.refreshTapDamage = function() {
            applyFilters(Const.ISBOSS, !1) ? this._tapDamageLabel.text = HeroController.getInstance().mainHero.bossDps: this._tapDamageLabel.text = HeroController.getInstance().mainHero.tapDamage
        };
        b.prototype.refreshView = function() {
            this._skillTableView && this._skillTableView.reloadData(this._dataList)
        };
        b.prototype.dataChange = function(a, b) {
            this._dataList[b] = a;
            this._skillTableView.changeDataListOnly(this._dataList)
        };
        b.prototype.createItemRenderer = function() {
            return new SkillItemView
        };
        b.prototype.updateItemRenderer = function(a, b, c) {
            a.refreshView(b, c)
        };
        b.prototype.dispose = function() {
            c.prototype.dispose.call(this);
            removeEventsByTarget(this)
        };
        return b
    } (BaseContainer);
SkillListLayer.prototype.__class__ = "SkillListLayer";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    AchieveItemLayer = function(c) {
        function b() {
            c.call(this);
            this._icon = null;
            this.initView()
        }
        __extends(b, c);
        b.prototype.initView = function() {
            this._bg = ResourceUtils.createBitmapFromSheet("hero_item_bg", "commonRes");
            this.addChild(this._bg);
            this._bg.x = 3;
            this._bg.y = 3;
            this._iconContainer = new egret.DisplayObjectContainer;
            this.addChild(this._iconContainer);
            this._iconContainer.x = 3;
            this._iconContainer.y = 3;
            this._container = new egret.DisplayObjectContainer;
            this._iconContainer.addChild(this._container);
            this._container.x = 25;
            this._container.y = 11;
            var a = ResourceUtils.createBitmapFromSheet("icon_decorate");
            a.anchorX = a.anchorY = 0.5;
            a.x = 60;
            a.y = 47;
            this._iconContainer.addChild(a);
            this._starContainer = new egret.DisplayObjectContainer;
            this._iconContainer.addChild(this._starContainer);
            this._nameLabel = new egret.TextField;
            this._nameLabel.x = 110;
            this._nameLabel.y = 15;
            this._nameLabel.size = 22;
            this._nameLabel.text = "\u540d\u79f0";
            this._iconContainer.addChild(this._nameLabel);
            this._nameLabel.textColor = 2495759;
            this._progressLabel = new egret.TextField;
            this._iconContainer.addChild(this._progressLabel);
            this._progressLabel.x = 290;
            this._progressLabel.y = 56;
            this._progressLabel.size = 20;
            this._progressLabel.textAlign = egret.HorizontalAlign.LEFT;
            this._progressLabel.text = "100/100";
            this._progressLabel.textColor = 2495759;
            this._gold = ResourceUtils.createBitmapFromSheet("gold_icon");
            this._iconContainer.addChild(this._gold);
            this._gold.scaleX = this._gold.scaleY = 0.5;
            this._gold.x = 490;
            this._gold.y = 8;
            this._awardLabel = new egret.TextField;
            this._iconContainer.addChild(this._awardLabel);
            this._awardLabel.x = 520;
            this._awardLabel.y = 8;
            this._awardLabel.size = 18;
            this._awardLabel.width = 80;
            this._awardLabel.text = "15";
            this._iconContainer.cacheAsBitmap = !0;
            this._button = new AchieveButton;
            this._button.addOnClick(this.onRewardHandler, this);
            this._button.useZoomOut(!0);
            this._button.x = 523;
            this._button.y = 62;
            this.addChild(this._button);
            this._finishIcon = ResourceUtils.createBitmapFromSheet("achieve_fh", "commonRes");
            this._finishIcon.anchorX = this._finishIcon.anchorY = 0.5;
            this._finishIcon.x = 523;
            this._finishIcon.y = 51;
            this._finishIcon.visible = !1;
            this.addChild(this._finishIcon);
            this._finishBorder = ResourceUtils.createBitmapFromSheet("achieve_fh_border", "commonRes");
            this._finishBorder.anchorX = this._finishBorder.anchorY = 0.5;
            this._finishBorder.x = 3 + this._bg.width / 2;
            this._finishBorder.y = 3 + this._bg.height / 2;
            this._finishBorder.visible = !1;
            this.addChild(this._finishBorder)
        };
        b.prototype.create = function(a, b) {
            var c = this;
            Time.setTimeout(function() {
                    var b = ResourceUtils.createBitmapFromSheet("gold_icon");
                    b.anchorX = 0.5;
                    b.anchorY = 0.5;
                    b.scaleX = b.scaleY = 0.5;
                    b.x = 520;
                    b.y = 49;
                    c.addChild(b);
                    b.num = a;
                    doAction(Const.ADD_DIAMOND, b)
                },
                this, b)
        };
        b.prototype.initStars = function() {
            GameUtils.removeAllChildren(this._starContainer);
            for (var a = this._model.index,
                     b = 0; 5 > b; b++) {
                var c = null,
                    c = a > b ? ResourceUtils.createBitmapFromSheet("star") : ResourceUtils.createBitmapFromSheet("star_bg");
                c.x = 120 + 35 * b;
                c.y = 65;
                c.anchorX = c.anchorY = 0.5;
                this._starContainer.addChild(c)
            }
        };
        b.prototype.getNumbers = function() {
            var a = parseInt(this._model.award),
                b = 20,
                c = 0,
                e = 0;
            a < b ? (b = a, e = 1) : (e = Math.floor(a / b), c = a % b);
            return [b, e, c]
        };
        b.prototype.onRewardHandler = function() {
            for (var a = this.getNumbers(), b = 0; b < a[0]; b++) {
                var c = a[1];
                b == a[0] - 1 && (c += a[2]);
                this.create(c.toString(), GameUtils.random(0, 1E3))
            }
            doAction(Const.ACHIEVE, this._model);
            DataCenter.getInstance().achieve.addValue(this._model.type);
            this.refreshView(this._model, this._idx)
        };
        b.prototype.refreshView = function(a, b) {
            this._model = a;
            this._idx = b;
            this._awardLabel.text = this._model.award;
            this._progressLabel.text = this._model.current + "/" + this._model.total;
            this._nameLabel.text = this._model.name;
            GameUtils.removeFromParent(this._icon);
            GameUtils.removeAllChildren(this._container);
            this._icon = ResourceUtils.createBitmapFromSheet(this._model.pic, "iconRes");
            this._container.addChild(this._icon);
            var c = this._model.isMax();
            this._button.visible = !c;
            this._awardLabel.visible = !c;
            this._gold.visible = !c;
            this._model.isMax() ? (this._button.visible = !1, this._finishBorder.visible = !0, this._finishIcon.visible = !0, this._gold.visible = !1, this._awardLabel.visible = !1) : (this._button.refreshText("\u5956\u52b1"), this._button.setEnabled(this._model.canAward()), this._finishBorder.visible = !1, this._finishIcon.visible = !1, this._gold.visible = !0, this._awardLabel.visible = !0);
            this.initStars()
        };
        return b
    } (BaseContainer);
AchieveItemLayer.prototype.__class__ = "AchieveItemLayer";
var AchieveButton = function(c) {
    function b() {
        c.call(this);
        this.anchorY = this.anchorX = 0.5;
        this.init()
    }
    __extends(b, c);
    b.prototype.setEnabledState = function() {
        this._nor.visible = this._enabled;
        this._dis.visible = !this._enabled
    };
    b.prototype.init = function() {
        this._container = new egret.DisplayObjectContainer;
        this.addChild(this._container);
        this._nor = ResourceUtils.createBitmapFromSheet("hero_lvup_btn");
        this._container.addChild(this._nor);
        this._dis = ResourceUtils.createBitmapFromSheet("hero_lvup_disable");
        this._dis.visible = !1;
        this._container.addChild(this._dis);
        this._label = new egret.TextField;
        this._container.addChild(this._label);
        this._label.text = "\u5956\u52b1";
        this._label.width = 150;
        this._label.x = 85;
        this._label.y = 20;
        this._label.size = 20;
        this._label.textAlign = egret.HorizontalAlign.CENTER;
        this._label.anchorX = 0.5;
        this._label.textColor = 2495759;
        this._container.cacheAsBitmap = !0
    };
    b.prototype.refreshText = function(a) {
        this._label.text = a
    };
    return b
} (SimpleButton);
AchieveButton.prototype.__class__ = "AchieveButton";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    TopItemLayer = function(c) {
        function b(a) {
            void 0 === a && (a = "top");
            c.call(this);
            this._icon = null;
            this.initView(a)
        }
        __extends(b, c);
        b.prototype.initView = function(a) {
            this._bg = "self" == a ? ResourceUtils.createBitmapFromSheet("top_light_item", "commonRes") : ResourceUtils.createBitmapFromSheet("hero_item_bg", "commonRes");
            this.addChild(this._bg);
            this._iconContainer = new egret.DisplayObjectContainer;
            this.addChild(this._iconContainer);
            this._lifeBorder = ResourceUtils.createBitmapFromSheet("life_border");
            this._lifeBorder.anchorX = this._lifeBorder.anchorY = 0.5;
            this._lifeBorder.x = 60;
            this._lifeBorder.y = 47;
            this._iconContainer.addChild(this._lifeBorder);
            this._container = new egret.DisplayObjectContainer;
            this._iconContainer.addChild(this._container);
            this._container.x = 25;
            this._container.y = 12;
            a = ResourceUtils.createBitmapFromSheet("icon_decorate");
            a.anchorX = a.anchorY = 0.5;
            a.x = 60;
            a.y = 47;
            this._iconContainer.addChild(a);
            this._nameLabel = new egret.TextField;
            this._nameLabel.x = 110;
            this._nameLabel.y = 12;
            this._nameLabel.size = 22;
            this._nameLabel.text = "\u7528\u6237\u540d\u79f0";
            this._iconContainer.addChild(this._nameLabel);
            this._nameLabel.textColor = 2495759;
            this._timeTitleLabel = new egret.TextField;
            this._timeTitleLabel.x = 110;
            this._timeTitleLabel.y = 51;
            this._timeTitleLabel.size = 22;
            this._timeTitleLabel.text = "\u5b8c\u6210\u5173\u5361:";
            this._iconContainer.addChild(this._timeTitleLabel);
            this._timeTitleLabel.textColor = 16777215;
            this._timelLabel = new egret.TextField;
            this._timelLabel.x = 220;
            this._timelLabel.y = 51;
            this._timelLabel.size = 22;
            this._timelLabel.text = "90";
            this._iconContainer.addChild(this._timelLabel);
            this._timelLabel.textColor = 16777215;
            this._rankbg = ResourceUtils.createBitmapFromSheet("top_rank_bg", "commonRes");
            this._rankbg.x = 550;
            this._rankbg.y = 49;
            this._rankbg.anchorX = this._rankbg.anchorY = 0.5;
            this._iconContainer.addChild(this._rankbg);
            this._rankContainer = new egret.DisplayObjectContainer;
            a = ResourceUtils.createBitmapFromSheet("top_rank_1", "commonRes");
            this._rankContainer.x = 550;
            this._rankContainer.y = 47;
            this._rankContainer.anchorX = this._rankContainer.anchorY = 0.5;
            this._rankContainer.addChild(a);
            this._iconContainer.addChild(this._rankContainer);
            this._iconContainer.cacheAsBitmap = !0;
            this._selfContainer = new egret.DisplayObjectContainer;
            this._selfContainer.x = 490;
            this._selfContainer.y = 0;
            this._selfTopTitleLabel = new egret.TextField;
            this._selfTopTitleLabel.x = 0;
            this._selfTopTitleLabel.y = 12;
            this._selfTopTitleLabel.size = 24;
            this._selfTopTitleLabel.text = "\u5f53\u524d\u6392\u540d\uff1a";
            this._selfTopTitleLabel.textColor = 2495759;
            this._selfContainer.addChild(this._selfTopTitleLabel);
            this._selfTopLabel = new egret.TextField;
            this._selfTopLabel.x = 43;
            this._selfTopLabel.y = 43;
            this._selfTopLabel.size = 32;
            this._selfTopLabel.text = "108";
            this._selfContainer.addChild(this._selfTopLabel);
            this._iconContainer.addChild(this._selfContainer)
        };
        b.prototype.refreshView = function(a, b) {
            this._model = a;
            this._idx = b;
            if ( - 1 == this._idx) this._rankContainer.visible = !1,
                this._selfContainer.visible = !0,
                this._rankbg.visible = !1,
                this._timeTitleLabel.text = "\u5b8c\u6210\u5173\u5361:",
                this._timelLabel.text = DataCenter.getInstance().totalData.currentLevel.toString(),
                this._selfTopLabel.text = this._model.top;
            else {
                this._rankbg.visible = !0;
                this._rankContainer.visible = !0;
                this._selfContainer.visible = !1;
                this._timelLabel.text = this._model.level;
                var c = ResourceUtils.createBitmapFromSheet("top_rank_" + this._model.top, "commonRes");
                this._rankContainer.removeChildren();
                this._rankContainer.addChild(c)
            }
            this._lifeBorder.visible = 0 < this._model.life ? !0 : !1;
            var e;
            "" != this._model.pic ? e = egret.DynamicBitmap.create(this._model.pic, !0,
                function() {
                    e.x = 35;
                    e.y = 35;
                    e.anchorX = e.anchorY = 0.5;
                    e.scaleX = 68 >= e.width ? 1 : 68 / e.width;
                    e.scaleY = 68 >= e.height ? 1 : 68 / e.height
                },
                this) : (e = ResourceUtils.createBitmapFromSheet("icon_hero_skill_1", "iconRes"), e.x = 35, e.y = 35, e.anchorX = e.anchorY = 0.5);
            this._container.removeChildren();
            this._container.addChild(e);
            this._nameLabel.text = this._model.name
        };
        return b
    } (BaseContainer);
TopItemLayer.prototype.__class__ = "TopItemLayer";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    AchieveEntryButton = function(c) {
        function b() {
            c.call(this);
            this.anchorX = this.anchorY = 0.5;
            this.init();
            this.useZoomOut(!0);
            addAction(Const.CHANGE_STATISTICS, this.changeStatistics, this)
        }
        __extends(b, c);
        b.prototype.init = function() {
            this._container = new egret.DisplayObjectContainer;
            this.addChild(this._container);
            var a = ResourceUtils.createBitmapFromSheet("cup");
            this._container.addChild(a);
            this._newFlag = ResourceUtils.createBitmapFromSheet("new_achieve");
            this._newFlag.x = 25;
            this._newFlag.visible = !1;
            this._container.addChild(this._newFlag);
            this.addOnClick(this.click, this);
            this.changeStatistics()
        };
        b.prototype.changeStatistics = function() {
            this._container.cacheAsBitmap = !1;
            this._newFlag.visible = DataCenter.getInstance().hasNewAchieve();
            this._container.cacheAsBitmap = !0
        };
        b.prototype.click = function() {
            GameMainLayer.instance.setAchieveLayerVisible()
        };
        return b
    } (SimpleButton);
AchieveEntryButton.prototype.__class__ = "AchieveEntryButton";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    BattleStatusButton = function(c) {
        function b() {
            c.call(this)
        }
        __extends(b, c);
        b.prototype.initView = function() {
            this.leaveBtn = new SimpleButton;
            this.leaveBtn.anchorX = this.leaveBtn.anchorY = 0.5;
            this.addChild(this.leaveBtn);
            var a = ResourceUtils.createBitmapFromSheet("leave_battle", "commonRes");
            this.leaveBtn.addChild(a);
            a = new egret.TextField;
            a.text = "\u79bb\u5f00";
            a.x = 56;
            a.y = 15;
            this.leaveBtn.addChild(a);
            this.leaveBtn.addOnClick(this.onLeaveHandler, this);
            this.leaveBtn.useZoomOut(!0);
            this.fightBtn = new SimpleButton;
            this.fightBtn.anchorX = this.fightBtn.anchorY = 0.5;
            this.addChild(this.fightBtn);
            a = ResourceUtils.createBitmapFromSheet("fight_boss", "commonRes");
            this.fightBtn.addChild(a);
            a = new egret.TextField;
            a.text = "BOSS";
            a.x = 41;
            a.y = 15;
            this.fightBtn.addChild(a);
            this.fightBtn.addOnClick(this.onFightHandler, this);
            this.fightBtn.useZoomOut(!0)
        };
        b.prototype.setStatus = function(a) {
            this.fightBtn.visible = 1 == a;
            this.leaveBtn.visible = 2 == a
        };
        b.prototype.onLeaveHandler = function() {
            doAction(Const.LEAVE_BATTLE)
        };
        b.prototype.onFightHandler = function() {
            doAction(Const.FIGHT_BOSS)
        };
        return b
    } (BaseContainer);
BattleStatusButton.prototype.__class__ = "BattleStatusButton";
var BattleStatus; (function(c) {
    c[c.fight = 1] = "fight";
    c[c.leave = 2] = "leave";
    c[c.normal = 3] = "normal"
})(BattleStatus || (BattleStatus = {}));
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    Boss = function(c) {
        function b() {
            c.call(this);
            this._isBox = this._isBoss = !1;
            this._idx = 0;
            this._locked = this._isShow = !1;
            this.init()
        }
        __extends(b, c);
        b.prototype.init = function() {
            this._hpBar = new HPProgress("progress_spr", "progress_back");
            this.addChild(this._hpBar);
            this._hpBar.setOffset(2, 2);
            this._movieContainer = new egret.DisplayObjectContainer;
            this.addChild(this._movieContainer);
            this._hpBar.x = 183;
            this._hpBar.y = 200;
            this._nameLabel = new egret.TextField;
            this._nameLabel.textColor = 0;
            this._nameLabel.size = 18;
            this.addChild(this._nameLabel);
            this._nameLabel.x = 187;
            this._nameLabel.y = 203;
            this._nameLabel.text = "\u540d\u5b57";
            this._hpLabel = new egret.TextField;
            this._hpLabel.textColor = 0;
            this.addChild(this._hpLabel);
            this._hpLabel.anchorX = 1;
            this._hpLabel.size = 18;
            this._hpLabel.x = 432;
            this._hpLabel.y = 203;
            this._hpLabel.text = "1000 HP";
            addFilter(Const.ISBOSS, this.isFightingBoss, this);
            addAction(Const.CHANGE_CLICK_COIN, this.initClickCoin, this);
            addAction(Const.GET_PAYMENT_EFFECT, this.getPaymentSkill, this)
        };
        b.prototype.getPaymentSkill = function() {
            var a = this.maxHP.times(0.5, "unset"),
                b = {};
            b.dmg = a;
            b.crit = !0;
            return b
        };
        b.prototype.isFightingBoss = function() {
            return this._isBoss
        };
        b.prototype.refreshBoss = function(a) {
            void 0 === a && (a = !0);
            this._action && (this._action.dispose(), this._movieContainer.removeChild(this._action), this._action = null);
            GameUtils.isBoss() && a ? this._isBoss = !0 : this._isBoss = !1;
            this._isBox = !1;
            if (!1 == this._isBoss) {
                var b = GameUtils.random(1, 100),
                    c = 5,
                    e = applyFilters(Const.GET_HOLY_EFFECT, getApplyString(21));
                e != getApplyString(21) && (c += e);
                b <= c && (this._isBox = !0)
            }
            doAction(Const.ENTER_FIGHT);
            var h = GameUtils.getMonsterID(this._isBoss);
            this._isBox && (h = 41);
            this._nameLabel.text = bossNameConfig[h - 1];
            b = ResourceUtils.createBitmapFromSheet("boss_default");
            b.x = 320;
            b.y = 640;
            b.anchorX = 0.5;
            b.anchorY = 1;
            b.alpha = 0.7;
            this._movieContainer.addChild(b);
            GameUtils.preloadAnimation("monster_" + h + "_json",
                function(a) {
                    1E3 * DataCenter.getInstance().totalData.currentLevel + DataCenter.getInstance().totalData.currentPass > a || (this._movieContainer.removeChildren(), this._action = new Movie("monster_" + h + "_json", "monster_" + h), this._action.play("stand"), this._action.autoRemove = !1, this._action.setCallback(this.onMoveCallback, this), this._movieContainer.addChild(this._action), !1 == this._isBoss && !1 == this._isBox && (this._action.scaleX = 0.6, this._action.scaleY = 0.6), this._action.y = 625, this._action.x = 320)
                },
                this, 1E3 * DataCenter.getInstance().totalData.currentLevel + DataCenter.getInstance().totalData.currentPass);
            b = DataCenter.getInstance().getBossHP(a, !1);
            this.curHP = new NumberModel;
            this.curHP.add(b);
            this.maxHP = new NumberModel;
            this.maxHP.add(b);
            this.randomCoin();
            e = applyFilters(Const.GET_HOLY_EFFECT, getApplyString(24));
            e != getApplyString(24) && (this.curHP.times(1 - e), this.maxHP.times(1 - e));
            this._hpLabel.text = this.curHP.getNumer() + " HP";
            this._hpBar.percentage = 100;
            this._passed = a;
            this._isBoss && DataCenter.getInstance().checkKillHero()
        };
        b.prototype.onMoveCallback = function() {
            for (var a = [], b = 0; b < arguments.length; b++) a[b - 0] = arguments[b];
            a[0] == Movie.PLAY_COMPLETE && this._action.play("stand")
        };
        b.prototype.attackedWithoutAction = function(a) {
            if (!this._locked) {
                DataCenter.getInstance().recordAttackDPS(a);
                a = this.curHP.reduce(a);
                var b = this.curHP.getNumer();
                this._hpLabel.text = b + "HP"; ! 1 == a || "0" == b ? (this.dropCoin(), this._hpBar.percentage = 0, this.dieEffect()) : this._hpBar.percentage = this.curHP.percent(this.maxHP.getData())
            }
        };
        b.prototype.randomCoin = function() {
            var a = GameUtils.random(45, 65),
                b = SkillController.getInstance().boxDropCoinTimes,
                c = 1;
            this._isBoss && applyFilters(Const.GET_HOLY_EFFECT, getApplyString(23)) != getApplyString(23) && (c += 1);
            var c = c * SkillController.getInstance().dropCoinTimes,
                c = c * SkillController.getInstance().dropCoinTimes1,
                e = applyFilters(Const.GET_HOLY_EFFECT, getApplyString(22));
            e != getApplyString(22) && Math.random() <= e / 100 && (c *= 10);
            a = this._isBox ? this.maxHP.times(20 * b * c / a, "unset") : this.maxHP.times(c / a, "unset");
            "0" == a && (a = "1");
            "1" == a ? this._dropNum = 1 : "2" == a ? (this._dropNum = 2, a = "1") : (this._dropNum = !1 == this._isBoss && !1 == this._isBox ? GameUtils.random(1, 2) : GameUtils.random(3, 4), b = new NumberModel, b.add(a), a = b.times(1 / this._dropNum), "0" == a && (a = "1"));
            this._dropCoin = a;
            this.initClickCoin()
        };
        b.prototype.initClickCoin = function() {
            this._clickCoin = "0";
            this._clickNum = 1;
            if (0 < HeroController.getInstance().mainHero.tapCoin) {
                var a = new NumberModel;
                a.add(this._dropCoin);
                a.times(this._dropNum * HeroController.getInstance().mainHero.tapCoin / this._clickNum);
                this._clickCoin = a.getNumer()
            }
        };
        b.prototype.dropCoin = function() {
            var a = {};
            a.one = this._dropCoin;
            a.num = this._dropNum;
            doAction(Const.DROP_COIN, a);
            PlatformFactory.getPlatform().name == PlatformTypes.WEIXIN && ProxyUtils.awdTo && !this._isShow && 0 == ProxyUtils.awdTo.show && (this._isShow = !0, doAction(Const.DROP_COIN, {
                one: "1",
                num: 1,
                type: 8
            }))
        };
        b.prototype.dropClickCoin = function() {
            var a = {};
            a.one = this._clickCoin;
            a.num = this._clickNum;
            doAction(Const.DROP_COIN, a)
        };
        b.prototype.attacked = function(a) {
            if (this._locked) return ! 1;
            DataCenter.getInstance().recordAttackDPS(a);
            a = this.curHP.reduce(a);
            var b = this.curHP.getNumer();
            this._hpLabel.text = b + "HP";
            "0" != this._clickCoin && this.dropClickCoin(); ! 1 == a || "0" == b ? (this.dropCoin(), this._hpBar.percentage = 0, this.dieEffect()) : this._hpBar.percentage = this.curHP.percent(this.maxHP.getData());
            this._action && this._action.play("injured");
            return ! 0
        };
        b.prototype.dieEffect = function() {
            var a = this;
            this._locked = !0;
            this._isBoss ? DataCenter.getInstance().statistics.addValue(StatisticsType.boss) : DataCenter.getInstance().statistics.addValue(StatisticsType.enemy);
            if (this._action) {
                var b = (new Date).getTime();
                GameUtils.preloadAnimation("boss_death_json",
                    function() {
                        if (500 > (new Date).getTime() - b && a._action) {
                            var c = new Movie("boss_death_json", "boss_death");
                            c.play("1");
                            var e = a._movieContainer.localToGlobal(a._action.x, a._action.y),
                                e = a.globalToLocal(e.x, e.y, e);
                            c.x = e.x;
                            c.y = e.y;
                            a.addChild(c)
                        }
                    });
                this._action.play("injured");
                this._action.gotoAndStop(1);
                egret.Tween.get(this._action).to({
                        alpha: 0
                    },
                    300).call(function() {
                        a._passed ? DataCenter.getInstance().bossLevelUp(a._isBoss, a._isBox) : doAction(Const.LEAVE_BATTLE);
                        a._locked = !1
                    },
                    this)
            } else this._passed ? DataCenter.getInstance().bossLevelUp(this._isBoss, this._isBox) : doAction(Const.LEAVE_BATTLE),
                this._locked = !1
        };
        b.prototype.dispose = function() {
            c.prototype.dispose.call(this);
            removeEventsByTarget(this)
        };
        return b
    } (BaseContainer);
Boss.prototype.__class__ = "Boss";
var bossNameConfig = "\u72ec\u773c\u602a \u77f3\u5934\u4eba \u6811\u4eba \u683c\u529b\u7279\u70ae\u5175 \u6b7b\u7075\u6cd5\u5e08 \u6c99\u6f20\u5f3a\u76d7 \u683c\u529b\u7279\u58eb\u5175 \u7329\u7329 \u54e5\u5e03\u6797 \u9ab7\u9ac5\u6218\u58eb \u86de\u8753\u4e38\u5b50 \u4f0a\u8299\u5229\u7279 \u6728\u4e43\u4f0a \u4ed9\u4eba\u638c\u602a \u683c\u529b\u7279\u9b54\u6cd5\u5e08 \u683c\u5229\u7279\u4fee\u7406\u5de5 \u683c\u5229\u7279\u8fd1\u536b\u5175 \u683c\u5229\u7279\u7687\u5bb6\u9a91\u58eb \u683c\u5229\u7279\u706b\u67aa\u624b \u683c\u529b\u7279\u673a\u68b0\u9a91\u58eb \u68ee\u6797\u5f3a\u76d7 \u683c\u5229\u7279\u7981\u519b \u767d\u7329\u7329 \u90e8\u843d\u5c04\u624b \u9ab7\u9ac5\u5175 \u4ed9\u4eba\u7403\u602a \u517d\u4eba\u5de5\u5320 \u683c\u5229\u7279\u796d\u7940 \u72c2\u66b4\u6728\u4e43\u4f0a \u7c89\u7ea2\u4f0a\u8299\u5229\u7279 \u5c3e\u517d\u4e38\u5b50 \u72ec\u773c\u5de8\u517d \u8346\u68d8\u602a \u67af\u6811\u4eba \u683c\u5229\u7279\u70ae\u624b \u683c\u5229\u7279\u9a91\u58eb \u683c\u5229\u7279\u5de8\u5251\u624b \u68ee\u6797\u730e\u4eba \u9ed1\u6697\u6cf0\u5766 \u5149\u660e\u796d\u7940 \u5b9d\u7bb1\u602a".split(" "),
    __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    BossLevelView = function(c) {
        function b() {
            c.call(this);
            this._lastLevel = 1;
            this.posX = [ - 80, -10, 60];
            this.posY = [0, 5, 0];
            this.init()
        }
        __extends(b, c);
        b.prototype.hideLevel = function() {
            this._lvLabel.visible = !1;
            this._monsterIcon.visible = !1
        };
        b.prototype.showLevel = function() {
            this._lvLabel.visible = !0;
            this._monsterIcon.visible = !0
        };
        b.prototype.init = function() {
            this._passContainer = new egret.DisplayObjectContainer;
            this._passContainer.x = 30;
            this.addChild(this._passContainer);
            var a = ResourceUtils.createBitmapFromSheet("pass_icon_bg", "commonRes");
            a.x = -116;
            a.y = -27;
            this._passContainer.addChild(a);
            this._bossIconContainer = new egret.DisplayObjectContainer;
            this.addChild(this._bossIconContainer);
            a = DataCenter.getInstance().totalData;
            this._monsterIcon = ResourceUtils.createBitmapFromSheet("monster_icon", "commonRes");
            this.addChild(this._monsterIcon);
            this._monsterIcon.x = 180;
            this._monsterIcon.y = -30;
            this._lvLabel = new egret.BitmapText;
            var b = RES.getRes("battle_num_fnt_fnt");
            this._lvLabel.font = b;
            this.addChild(this._lvLabel);
            this._lvLabel.x = 225;
            this._lvLabel.y = -25;
            this._lvLabel.text = a.currentPass + "/" + GameUtils.getMaxCustomPass();
            this._lastLevel = a.currentLevel;
            this.initPassContainer();
            this._passContainer.cacheAsBitmap = !0;
            addAction(Const.REBORN_SKILL_REFRESH, this.refreshView, this)
        };
        b.prototype.initPassContainer = function() {
            var a = DataCenter.getInstance().totalData,
                b;
            1 == a.currentLevel ? (b = new LevelIcon(1), b.setLabelAlpha(), b.x = this.posX[1], b.y = this.posY[1], this._passContainer.addChild(b), b = new LevelIcon(2), b.x = this.posX[2], b.y = this.posY[2], this._passContainer.addChild(b), b.scaleX = b.scaleY = 0.8) : a.currentLevel == Const.maxLevel ? (b = new LevelIcon(Const.maxLevel - 1), b.x = this.posX[0], b.y = this.posY[0], this._passContainer.addChild(b), b.scaleX = b.scaleY = 0.8, b = new LevelIcon(Const.maxLevel), b.x = this.posX[1], b.y = this.posY[1], this._passContainer.addChild(b), b.setLabelAlpha()) : (b = new LevelIcon(a.currentLevel - 1), b.x = this.posX[0], b.y = this.posY[0], this._passContainer.addChild(b), b.scaleX = b.scaleY = 0.8, b.setLabelAlpha(), b = new LevelIcon(a.currentLevel), b.x = this.posX[1], b.y = this.posY[1], this._passContainer.addChild(b), a = new LevelIcon(a.currentLevel + 1), a.x = this.posX[2], a.y = this.posY[3], this._passContainer.addChild(a), a.scaleX = a.scaleY = 0.8, a.setLabelAlpha())
        };
        b.prototype.refreshView = function() {
            var a = DataCenter.getInstance().totalData;
            a.currentPass >= GameUtils.getMaxCustomPass() ? this._lvLabel.text = a.currentPass + "/" + a.currentPass: this._lvLabel.text = a.currentPass + "/" + GameUtils.getMaxCustomPass();
            a.currentLevel > this._lastLevel ? (this._bossIconContainer.removeChildren(), this._lastLevel = a.currentLevel, this.playAnimation(), DataCenter.getInstance().statistics.addValue(StatisticsType.level)) : 1 == a.currentLevel && (this._lastLevel = a.currentLevel, this._passContainer.removeChildren(), a = ResourceUtils.createBitmapFromSheet("pass_icon_bg", "commonRes"), a.x = -116, a.y = -27, this._passContainer.addChild(a), this.initPassContainer())
        };
        b.prototype.playAnimation = function() {
            var a = this;
            this._passContainer.cacheAsBitmap = !1;
            var b = DataCenter.getInstance().totalData;
            if (2 == b.currentLevel) {
                var c = this._passContainer.getChildAt(1),
                    e = this._passContainer.getChildAt(2);
                egret.Tween.get(c).to({
                        x: this.posX[0],
                        y: this.posY[0],
                        scaleX: 0.8,
                        scaleY: 0.8
                    },
                    100).call(function() {
                        c.setLabelAlpha();
                        a._passContainer.cacheAsBitmap = !0
                    });
                egret.Tween.get(e).to({
                        x: this.posX[1],
                        y: this.posY[1],
                        scaleX: 1,
                        scaleY: 1
                    },
                    100).call(function() {
                        e.setLabelAlpha()
                    });
                var h = new LevelIcon(3);
                h.x = 130;
                this._passContainer.addChild(h);
                h.scaleX = h.scaleY = 0.6;
                egret.Tween.get(h).to({
                        x: this.posX[2],
                        y: this.posY[2],
                        scaleX: 0.8,
                        scaleY: 0.8
                    },
                    100).call(function() {
                        h.setLabelAlpha()
                    })
            } else if (b.currentLevel == Const.maxLevel) {
                var c = this._passContainer.getChildAt(1),
                    e = this._passContainer.getChildAt(2),
                    f = this._passContainer.getChildAt(3);
                egret.Tween.get(c).to({
                        x: -150,
                        scaleX: 0.6,
                        scaleY: 0.6
                    },
                    100).call(function() {
                        GameUtils.removeFromParent(c)
                    },
                    this);
                egret.Tween.get(e).to({
                        x: this.posX[0],
                        y: this.posY[0],
                        scaleX: 0.8,
                        scaleY: 0.8
                    },
                    100).call(function() {
                        e.setLabelAlpha();
                        a._passContainer.cacheAsBitmap = !0
                    });
                egret.Tween.get(f).to({
                        x: this.posX[1],
                        y: this.posY[1],
                        scaleX: 1,
                        scaleY: 1
                    },
                    100).call(function() {
                        f.setLabelAlpha()
                    })
            } else c = this._passContainer.getChildAt(1),
                e = this._passContainer.getChildAt(2),
                f = this._passContainer.getChildAt(3),
                egret.Tween.get(c).to({
                        x: -150,
                        scaleX: 0.6,
                        scaleY: 0.6
                    },
                    100).call(function() {
                        GameUtils.removeFromParent(c)
                    },
                    this),
                egret.Tween.get(e).to({
                        x: this.posX[0],
                        y: this.posY[0],
                        scaleX: 0.8,
                        scaleY: 0.8
                    },
                    100).call(function() {
                        e.setLabelAlpha()
                    }),
                egret.Tween.get(f).to({
                        x: this.posX[1],
                        y: this.posY[1],
                        scaleX: 1,
                        scaleY: 1
                    },
                    100).call(function() {
                        f.setLabelAlpha()
                    }),
                h = new LevelIcon(b.currentLevel + 1),
                h.x = 130,
                this._passContainer.addChild(h),
                h.scaleX = h.scaleY = 0.6,
                egret.Tween.get(h).to({
                        x: this.posX[2],
                        y: this.posY[2],
                        scaleX: 0.8,
                        scaleY: 0.8
                    },
                    100).call(function() {
                        h.setLabelAlpha();
                        a._passContainer.cacheAsBitmap = !0
                    })
        };
        b.prototype.dispose = function() {
            c.prototype.dispose.call(this);
            removeEventsByTarget(this)
        };
        return b
    } (BaseContainer);
BossLevelView.prototype.__class__ = "BossLevelView";
var LevelIcon = function(c) {
    function b(a) {
        c.call(this);
        this._level = a;
        this.anchorX = this.anchorY = 0.5;
        this.init(a)
    }
    __extends(b, c);
    b.prototype.init = function(a) {
        this._label = new egret.TextField;
        this.addChild(this._label);
        this._label.anchorX = 0.5;
        this._label.text = a.toString();
        this._label.strokeColor = 0;
        this._label.stroke = 1;
        this._label.x = 26;
        this._label.y = -30;
        a = GameUtils.getMapRes(a);
        this._icon = ResourceUtils.createBitmapFromSheet("icon_home_" + a, "iconRes");
        this.addChild(this._icon)
    };
    b.prototype.setLabelAlpha = function() {
        this._label.alpha = 1 == this._label.alpha ? 0.8 : 1
    };
    return b
} (BaseContainer);
LevelIcon.prototype.__class__ = "LevelIcon";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    CoinView = function(c) {
        function b() {
            c.call(this);
            this._coinList = [];
            this._typeSkill = {
                3 : 22002,
                4 : 22003,
                5 : 22004,
                6 : 22005
            };
            this._isTargetEffect = !1;
            this.init()
        }
        __extends(b, c);
        b.prototype.init = function() {
            this._coin = ResourceUtils.createBitmapFromSheet("coin_icon", "commonRes");
            this.addChild(this._coin);
            this._coin.anchorX = this._coin.anchorY = 0.5;
            this._coin.y = 80;
            this._coin.x = 20;
            this._coinLabel = new egret.BitmapText;
            var a = RES.getRes("battle_num_fnt_fnt");
            this._coinLabel.font = a;
            this.addChild(this._coinLabel);
            this._coinLabel.x = 50;
            this._coinLabel.y = 65;
            this._coinLabel.text = DataCenter.getInstance().getScNum();
            this._coinContainer = new egret.DisplayObjectContainer;
            this.addChild(this._coinContainer);
            this._goldcoin = ResourceUtils.createBitmapFromSheet("gold_icon", "commonRes");
            this.addChild(this._goldcoin);
            this._goldcoin.anchorX = this._coin.anchorY = 0.5;
            this._goldcoin.x = 270;
            this._goldcoin.y = 96;
            this._goldcoin.scaleX = this._goldcoin.scaleY = 0.8;
            this._goldcoin.anchorX = this._goldcoin.anchorY = 0.5;
            this._goldcoinLabel = new egret.BitmapText;
            a = RES.getRes("battle_num_fnt_fnt");
            this._goldcoinLabel.font = a;
            this.addChild(this._goldcoinLabel);
            this._goldcoinLabel.x = 290;
            this._goldcoinLabel.y = 85;
            this._goldcoinLabel.scaleX = this._goldcoinLabel.scaleY = 0.7;
            this._goldcoinLabel.text = DataCenter.getInstance().getGcNum();
            this._goldcoin.visible = !1;
            this._goldcoinLabel.visible = !1;
            this._holycoin = ResourceUtils.createBitmapFromSheet("holy_icon", "commonRes");
            this.addChild(this._holycoin);
            this._holycoin.anchorX = this._coin.anchorY = 0.5;
            this._holycoin.y = 80;
            this._holycoin.x = 270;
            this._holycoin.scaleX = this._holycoin.scaleY = 0.8;
            this._holycoinLabel = new egret.BitmapText;
            a = RES.getRes("battle_num_fnt_fnt");
            this._holycoinLabel.font = a;
            this.addChild(this._holycoinLabel);
            this._holycoinLabel.x = 290;
            this._holycoinLabel.y = 80;
            this._holycoinLabel.text = DataCenter.getInstance().getHolyNum().toString();
            this._holycoin.visible = !1;
            this._holycoinLabel.visible = !1;
            a = ResourceUtils.createBitmapFromSheet("suprise");
            a.anchorX = a.anchorY = 0.5;
            a.x = a.y = 40;
            this._supriseBtn = new SimpleButton;
            this._supriseBtn.x = 360;
            this._supriseBtn.y = 70;
            this._supriseBtn.width = 80;
            this._supriseBtn.height = 80;
            this._supriseBtn.useZoomOut(!0);
            this._supriseBtn.anchorX = this._supriseBtn.anchorY = 0.5;
            this._supriseBtn.addOnClick(this.clickSuprise, this);
            this._supriseBtn.addChild(a);
            this._supriseBtn.visible = !1;
            this.addChild(this._supriseBtn);
            addAction(Const.FLY_COIN, this.flyTo, this);
            addAction(Const.DROP_COIN, this.dropCoinEffect, this);
            addAction(Const.GET_ALL_COIN, this.addCoinEffect, this);
            addAction(Const.CHANGE_COIN, this.changeCoin, this);
            addAction(Const.CHANGE_DIAMOND, this.changeGoldCoin, this);
            addAction(Const.ADD_COIN, this.addCoin, this);
            addAction(Const.OPEN_BOX, this.openBox, this);
            addAction(Const.RESET_GAME, this.resetGame, this);
            addAction(Const.HIDE_GOLDCOIN, this.hideGoldCoin, this);
            addAction(Const.HIDE_HOLYCOIN, this.hideHolyCoin, this);
            addAction(Const.SHOW_HOLYCOIN, this.addHolyNum, this);
            addAction(Const.CHANGE_HOLY_NUM, this.changeHolyNum, this);
            addAction(Const.AWARD_EFFECT, this.awardEffect, this);
            this.setSuprise()
        };
        b.prototype.setSuprise = function() {
            PlatformFactory.getPlatform().name == PlatformTypes.WEIXIN && (this._supriseBtn.visible = ProxyUtils.awdTo && 1 == ProxyUtils.awdTo.show)
        };
        b.prototype.clickSuprise = function() {
            GameMainLayer.instance.showSupriseLayer()
        };
        b.prototype.changeHolyNum = function() {
            this._holycoinLabel.text = DataCenter.getInstance().getHolyNum().toString()
        };
        b.prototype.addHolyNum = function() {
            this._holycoin.visible = !0;
            this._holycoinLabel.visible = !0;
            doAction(Const.DROP_COIN, {
                type: 3,
                num: 1
            })
        };
        b.prototype.openBox = function(a, b) {
            var c = this.randBoxType();
            1 == c.type || 2 == c.type ? (doAction(Const.DROP_COIN, c, a, b), doAction(Const.SHOWBOXDESC, c)) : 8 == c.type ? doAction(Const.DROP_COIN, c, a, b) : 7 == c.type ? (c = this.getShareAward(), GameMainLayer.instance.showShareLayer(c)) : ((new BoxSkill(c.id)).execute(), doAction(Const.SHOWBOXDESC, c))
        };
        b.prototype.randBoxType = function() {
            var a = null,
                a = PlatformFactory.getPlatform().name == PlatformTypes.WEIXIN ? GameUtils.random(1, 7) : GameUtils.random(1, 6);
            2 < a && !1 == applyFilters(Const.IS_SKILL_USING, this._typeSkill[a]) && (a = 1);
            var b = {};
            1 == a ? (b.num = GameUtils.random(5, 8), a = this.getFallGold(), b.total = a.getNumer(), a.times(1 / b.num), a = a.getNumer(), b.one = "0" == a ? "1": a, b.type = 1) : 2 == a ? (b.num = 1, b.one = "1", b.type = 2) : 7 == a ? (b.num = 1, b.type = 7) : 8 == a ? (b.num = 1, b.type = 8) : (b.id = this._typeSkill[a], b.type = a);
            return b
        };
        b.prototype.getShareAward = function() {
            if (2 == GameUtils.random(1, 2)) {
                var a = new Date,
                    a = a.getFullYear().toString() + a.getMonth().toString() + a.getDate();
                a != DataCenter.getInstance().totalData.shareFalls.date && (DataCenter.getInstance().totalData.shareFalls.date = a, DataCenter.getInstance().totalData.shareFalls.num = 0);
                if (3 > DataCenter.getInstance().totalData.shareFalls.num) return a = {
                    title: "\u798f\u5229\u6765\u5570",
                    type: "diamond",
                    award: 10,
                    ad: !0
                },
                    DataCenter.getInstance().totalData.shareFalls.num += 1,
                    a
            }
            var a = {
                    title: "\u798f\u5229\u6765\u5570",
                    type: "gold"
                },
                b = this.getFallGold();
            a.award = b.times(4);
            a.ad = !0;
            return a
        };
        b.prototype.getFallGold = function() {
            var a = HeroController.getInstance().mainHero.dps,
                b = DataCenter.getInstance().getBossHP(),
                c = new NumberModel;
            c.add(b);
            c.compare(a) && (c.add(a), a = 30 < DataCenter.getInstance().totalData.currentLevel ? 0.35 : 0.05 + 0.01 * DataCenter.getInstance().totalData.currentLevel, c.times(a));
            return c
        };
        b.prototype.hideGoldCoin = function() {
            this._goldcoin.visible = !1;
            this._goldcoinLabel.visible = !1
        };
        b.prototype.hideHolyCoin = function() {
            this._holycoin.visible = !1;
            this._holycoinLabel.visible = !1
        };
        b.prototype.addCoin = function(a) {
            var b = a.parent.localToGlobal(a.x, a.y),
                b = this.globalToLocal(b.x, b.y, b);
            a.x = b.x;
            a.y = b.y;
            this.addChild(a);
            this.flyTo2(a)
        };
        b.prototype.changeCoin = function() {
            this._coinLabel.text = DataCenter.getInstance().getScNum()
        };
        b.prototype.changeGoldCoin = function() {
            this._goldcoinLabel.text = DataCenter.getInstance().getGcNum()
        };
        b.prototype.addCoinTxt = function() {};
        b.prototype.flyTo2 = function(a) {
            var b = GameUtils.random(250, 360),
                c = a.x + 400 * Math.sin(b),
                b = a.y + 400 * Math.sin(b);
            TweenLite.to(a, 1.5, {
                bezier: {
                    values: [{
                        x: c,
                        y: b
                    },
                        {
                            x: this._coin.x,
                            y: this._coin.y
                        }],
                    autoRotation: !0
                },
                onComplete: function() {
                    GameUtils.removeFromParent(a);
                    DataCenter.getInstance().changeSc(a.num)
                }
            })
        };
        b.prototype.flyTo = function(a) {
            var b = GameUtils.getCoinTxt(a.num);
            b.x = a.x;
            b.y = a.y - 50;
            b.start();
            this.addChild(b);
            var c = null,
                c = 2 == a.type ? this._goldcoin: 3 == a.type ? this._holycoin: 8 == a.type ? this._supriseBtn: this._coin;
            c.__sx || c.__sy || (c.__sx = c.scaleX, c.__sy = c.scaleY);
            var b = c.x > c.x ? 1 : -1,
                e = this;
            TweenLite.to(a, 1, {
                bezier: [{
                    x: a.x + GameUtils.random(150, 200) * b,
                    y: a.y - GameUtils.random(150, 200)
                },
                    {
                        x: c.x,
                        y: c.y
                    }],
                onComplete: function(b) {
                    var d = this;
                    this._isTargetEffect || (this._isTargetEffect = !0, egret.Tween.get(c).to({
                            scaleX: c.__sx + 0.2,
                            scaleY: c.__sy + 0.2
                        },
                        100).to({
                            scaleX: c.__sx,
                            scaleY: c.__sy
                        },
                        100).call(function() {
                            d._isTargetEffect = !1
                        }));
                    GameUtils.removeFromParent(a);
                    b == DataCenter.getInstance().totalData.currentLife && (2 == a.type ? (DataCenter.getInstance().changeGc(a.num), a.updated ? Time.setTimeout(function() {
                            doAction(Const.HIDE_GOLDCOIN)
                        },
                        this, 1E3) : (Time.setTimeout(function() {
                            doAction(Const.HIDE_GOLDCOIN)
                        },
                        this, 1E3), ProxyUtils.getDiamond("fall"))) : 3 == a.type ? (DataCenter.getInstance().changeHolyNum(1), Time.setTimeout(function() {
                            doAction(Const.HIDE_HOLYCOIN)
                        },
                        this, 1E3)) : 8 == a.type ? (e._supriseBtn.visible = !0, ProxyUtils.awdTo && (ProxyUtils.awdTo.show = 1)) : DataCenter.getInstance().changeSc(a.num))
                },
                onCompleteParams: [DataCenter.getInstance().totalData.currentLife]
            })
        };
        b.prototype.awardEffect = function(a) {
            var b = 0,
                b = "gold" == a.type ? 1 : 2,
                c = a.award,
                e = new NumberModel;
            e.add(c);
            a = {
                coins: e.pieces(5),
                type: b,
                updated: a.updated
            };
            doAction(Const.DROP_COIN, a, 320, 520)
        };
        b.prototype.addCoinEffect = function() {
            for (var a = 0; this._coinList.length;) this.openAwardBox(this._coinList.shift(), 20 * a++)
        };
        b.prototype.resetGame = function() {
            for (; this._coinList.length;) {
                var a = this._coinList.shift();
                a.cleanUp();
                a.parent && a.parent.removeChild(a)
            }
        };
        b.prototype.dropCoinEffect2 = function(a, b, c) {
            2 == a.type && (this._goldcoin.visible = !0, this._goldcoinLabel.visible = !0);
            var e, h;
            b ? (e = this.globalToLocal(b, c), b = Math.max(0, e.x - 30), e = Math.min(GameUtils.getWinWidth(), e.x + 30), c -= 80, h = 1) : (c = 147, b = 20, e = 150, h = 2);
            var f = null;
            if (a.coins) f = a.coins;
            else for (var f = [], k = 0; k < a.num; k++) f.push(a.one);
            for (k = 0; k < f.length; k++) {
                var l = new CoinContainer(f[k], a.type);
                l.updated = a.updated || !1;
                this.addChild(l);
                l.y = c;
                l.x = GameUtils.random(b, e);
                1 == h ? l.showAnimation1() : l.showAnimation();
                l.setCallback(this.openAwardBox, this);
                this._coinList.push(l)
            }
        };
        b.prototype.dropCoinEffect = function(a, b, c) {
            var e = this;
            if (8 == a.type && ProxyUtils.awdTo) {
                ProxyUtils.getlt(ProxyUtils.awdTo.type,
                    function(f) {
                        null != f && (ProxyUtils.awdTo.code = f, doAction(Const.CHANGE_DEFAULT_SHARE, ProxyUtils.awdTo), e.dropCoinEffect2(a, b, c))
                    });
                var h = GameUtils.random(3, 6),
                    f = {};
                f.id = this._typeSkill[h];
                f.type = h;
                this.dropCoinEffect2(f, b, c)
            } else this.dropCoinEffect2(a, b, c)
        };
        b.prototype.openAwardBox = function(a, b) {
            void 0 === b && (b = 0);
            var c = this._coinList.indexOf(a); - 1 < c && this._coinList.splice(c, 1);
            a.open(b)
        };
        b.prototype.dispose = function() {
            c.prototype.dispose.call(this);
            removeEventsByTarget(this)
        };
        return b
    } (BaseContainer);
CoinView.prototype.__class__ = "CoinView";
var test1 = function() {
        doAction(Const.DROP_COIN, {
            num: 1,
            one: "1",
            type: "1"
        })
    },
    test3 = function() {
        doAction(Const.OPEN_BOX, {
            num: 1,
            one: "1",
            type: "2"
        })
    },
    test2 = function() {
        DataCenter.getInstance().totalData.currentLevel += 1;
        doAction(Const.REFRESH_BOSS)
    },
    __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    ElfView = function(c) {
        function b() {
            c.call(this);
            this._delay = 0;
            this._itemsTouch = this._curElf = null;
            this._timeid = 0;
            this._clicked = !1;
            this._itemsTouch = new ItemsTouchManager;
            this._itemsTouch.setController(new TouchController(this.click, this));
            this.begin();
            addAction("test20", this.showElf, this)
        }
        __extends(b, c);
        b.prototype.showElf = function() {
            var a = this;
            GameUtils.preloadAnimation("elf_json",
                function() {
                    a._curElf = new Movie("elf_json", "elf");
                    a._spr = new egret.DisplayObjectContainer;
                    var b = new egret.Sprite;
                    b.anchorX = a._spr.anchorY = 0.5;
                    b.graphics.beginFill(16711680);
                    b.graphics.drawRect(0, 0, 70, 70);
                    b.graphics.endFill();
                    b.width = 70;
                    b.alpha = 0;
                    b.height = 70;
                    b.y = 5;
                    b.x = 30;
                    a._spr.addChild(b);
                    a._spr.addChild(a._curElf);
                    a._curElf.play("1");
                    a.addChild(a._spr);
                    a._spr.y = -50;
                    a._spr.x = GameUtils.random(0, 600);
                    a._delay = GameUtils.random(1, 10);
                    a._itemsTouch.addItems(b);
                    TweenLite.to(a._spr, 1, {
                        x: 320 + 200 * Math.sin(a._delay),
                        y: 200 + 50 * Math.cos(a._delay),
                        onComplete: function() {
                            a.addEventListener(egret.Event.ENTER_FRAME, a.moveElf, a);
                            a._timeid = Time.setTimeout(a.moveBack, a, 8E3)
                        }
                    })
                })
        };
        b.prototype.click = function() {
            var a = this;
            console.log("CLICK");
            if (!this._clicked) {
                this._clicked = !0;
                this._itemsTouch.removeItems(this._curElf);
                this._curElf.play("2");
                DataCenter.getInstance().statistics.addValue(StatisticsType.gift);
                var b = ResourceUtils.createBitmapFromSheet("box_normal");
                this.addChild(b);
                b.x = this._spr.x;
                b.y = this._spr.y;
                b.anchorX = b.anchorY = 0.5;
                b.scaleX = b.scaleY = 0.8;
                egret.Tween.get(b).to({
                        y: 516
                    },
                    1500).call(function() {
                        egret.Tween.removeTweens(b);
                        GameUtils.removeFromParent(b);
                        var c = ResourceUtils.createBitmapFromSheet("box_open");
                        c.x = b.x;
                        c.y = b.y;
                        c.anchorX = c.anchorY = 0.5;
                        c.scaleX = c.scaleY = 0.8;
                        a.addChild(c);
                        DataCenter.getInstance().statistics.addValue(StatisticsType.openbox);
                        var e = a.localToGlobal(b.x, b.y);
                        doAction(Const.OPEN_BOX, e.x, e.y);
                        egret.Tween.get(c).wait(1E3).to({
                                alpha: 0
                            },
                            200).call(function() {
                                GameUtils.removeFromParent(c)
                            })
                    });
                egret.Tween.get(b, {
                    loop: !0
                }).to({
                        rotation: 10
                    },
                    250).to({
                        rotation: 0
                    },
                    250).to({
                        rotation: -10
                    },
                    250).to({
                        rotation: 0
                    },
                    250);
                this.moveBack()
            }
        };
        b.prototype.moveBack = function() {
            var a = this;
            Time.clearTimeout(this._timeid);
            this.removeEventListener(egret.Event.ENTER_FRAME, this.moveElf, this);
            TweenLite.to(this._spr, 1, {
                x: GameUtils.random(0, 600),
                y: -50,
                onComplete: function() {
                    GameUtils.removeFromParent(a._spr);
                    GameUtils.removeFromParent(a._curElf);
                    a._curElf = null;
                    a._spr = null;
                    a.begin();
                    a._clicked = !1
                }
            })
        };
        b.prototype.begin = function() {
            Time.setTimeout(this.showElf, this, 12E4)
        };
        b.prototype.moveElf = function() {
            this._delay += 0.02;
            this._spr.y = 200 + 50 * Math.cos(this._delay);
            this._spr.x = 320 + 200 * Math.sin(this._delay)
        };
        return b
    } (BaseContainer);
ElfView.prototype.__class__ = "ElfView";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    OfflineView = function(c) {
        function b(a, b) {
            c.call(this);
            this._numModel = b;
            this._numModel.times(0.1);
            var g = a / 3600;
            this._time = a;
            this._box = 1 > g ? ResourceUtils.createBitmapFromSheet("offline_box_1") : 2 > g ? ResourceUtils.createBitmapFromSheet("offline_box_2") : ResourceUtils.createBitmapFromSheet("offline_box_3");
            this._box.anchorX = this._box.anchorY = 0.5;
            this._light = ResourceUtils.createBitmapFromSheet("offline_light");
            this._light.anchorX = this._light.anchorY = 0.5;
            egret.Tween.get(this._light, {
                loop: !0
            }).to({
                    rotation: 360
                },
                1E3);
            this.addChild(this._box);
            this.addChild(this._light);
            this._itemsTouch = new ItemsTouchManager;
            this._itemsTouch.setController(new TouchController(this.click, this));
            this._itemsTouch.addItems(this._box)
        }
        __extends(b, c);
        b.prototype.create = function(a, b) {
            var c = this;
            Time.setTimeout(function() {
                    var b = ResourceUtils.createBitmapFromSheet("coin_icon", "commonRes");
                    b.anchorX = 0.5;
                    b.anchorY = 0.5;
                    c.addChild(b);
                    b.num = a;
                    doAction(Const.ADD_COIN, b);
                    c._num--;
                    0 == c._num && egret.Tween.get(c).to({
                            alpha: 0
                        },
                        500).call(function() {
                            GameUtils.removeFromParent(c)
                        })
                },
                this, b)
        };
        b.prototype.click = function() {
            this._itemsTouch.dispose();
            this._itemsTouch = null;
            this._num = 10;
            for (var a = 0; a < this._num; a++) this.create(this._numModel.getNumer(), GameUtils.random(0, 1E3));
            14400 <= this._time && doAction(Const.SHOW_TIPS, "\u6700\u591a\u9886\u53d64\u5c0f\u65f6\u79bb\u7ebf\u5956\u52b1");
            a = GameUtils.getCoinTxt(this._numModel.times(10, "get"));
            a.x = this._box.x + 40;
            a.y = this._box.y;
            a.anchorX = a.anchorY = 0;
            a.start1(a.y - 80);
            this.addChild(a)
        };
        b.prototype.dispose = function() {
            c.prototype.dispose.call(this);
            this._itemsTouch && this._itemsTouch.dispose();
            this._itemsTouch = null
        };
        return b
    } (BaseContainer);
OfflineView.prototype.__class__ = "OfflineView";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    SetButton = function(c) {
        function b() {
            c.call(this);
            this.init()
        }
        __extends(b, c);
        b.prototype.init = function() {
            var a = ResourceUtils.createBitmapFromSheet("set_icon", "commonRes");
            this.addChild(a);
            this.addOnClick(this.onClickHandler, this);
            this.useZoomOut(!0)
        };
        b.prototype.onClickHandler = function() {
            GameMainLayer.instance.showSetLayer()
        };
        return b
    } (SimpleButton);
SetButton.prototype.__class__ = "SetButton";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    WeixinButton = function(c) {
        function b() {
            c.call(this);
            this.init();
            this.anchorX = this.anchorY = 0.5
        }
        __extends(b, c);
        b.prototype.init = function() {
			// 屏蔽微信关注
            // var a = ResourceUtils.createBitmapFromSheet("weixin", "commonRes");
            // this.addChild(a);
            // this.addOnClick(this.onClickHandler, this);
            // this.useZoomOut(!0)
        };
        b.prototype.onClickHandler = function() {
            GameMainLayer.instance.showCodeAwardLayer()
        };
        return b
    } (SimpleButton);
WeixinButton.prototype.__class__ = "WeixinButton";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    Main = function(c) {
        function b() {
            c.call(this);
            this._loaded = this._inited = !1;
            this._listener = null;
            this.addEventListener(egret.Event.ADDED_TO_STAGE, this.onAddToStage, this);
            addAction(Const.INFO_INITED, this.inited, this);
            this._listener = new ProxyListener
        }
        __extends(b, c);
        b.prototype.onAddToStage = function(a) {
            RES.addEventListener(RES.ResourceEvent.CONFIG_COMPLETE, this.onConfigComplete, this);
            egret.Injector.mapClass(RES.AnalyzerBase, RES.JsonSheetAnalyzer, "jsonsheet");
            RES.loadConfig("resource/resource.json", "resource/")
        };
        b.prototype.onConfigComplete = function(a) {
            RES.removeEventListener(RES.ResourceEvent.CONFIG_COMPLETE, this.onConfigComplete, this);
            RES.addEventListener(RES.ResourceEvent.GROUP_COMPLETE, this.onResourceLoadComplete, this);
            RES.addEventListener(RES.ResourceEvent.GROUP_PROGRESS, this.onResourceProgress, this);
            RES.loadGroup("loading")
        };
        b.prototype.onResourceLoadComplete = function(a) {
            "preload" == a.groupName ? (RES.removeEventListener(RES.ResourceEvent.GROUP_COMPLETE, this.onResourceLoadComplete, this), RES.removeEventListener(RES.ResourceEvent.GROUP_PROGRESS, this.onResourceProgress, this), this.createGameScene()) : "loading" == a.groupName && (PlatformFactory.setPlatform(GameUtils.getPlatformType()), PlatformFactory.getPlatform().init(), setCacheInfoFunction(), (a = document.getElementById("loadingDiv1")) && a.parentNode.removeChild(a), window.showPro && window.showPro(100), this.loadingView = new LoadingUI, this.stage.addChild(this.loadingView))
        };
        b.prototype.onResourceProgress = function(a) {
            "preload" == a.groupName && this.loadingView.setProgress(a.itemsLoaded, a.itemsTotal)
        };
        b.prototype.inited = function() {
            this._inited = !0;
            this.loaded()
        };
        b.prototype.loaded = function() {
            if (this._inited && this._loaded) {
                this.stage.removeChild(this.loadingView);
                DataCenter.getInstance().initGameData(PlatformFactory.getPlatform().deserialize());
                var a = new GameMainLayer;
                this.addChild(a);
                SoundManager.getInstance().playBackgroundMusic()
            }
        };
        b.prototype.createGameScene = function() {
            this._loaded = !0;
            this.loaded()
        };
        b.prototype.createBitmapByName = function(a) {
            var b = new egret.Bitmap;
            a = RES.getRes(a);
            b.texture = a;
            return b
        };
        b.prototype.startAnimation = function(a) {
            var b = this.textContainer,
                c = -1,
                e = this,
                h = function() {
                    c++;
                    c >= a.length && (c = 0);
                    e.changeDescription(b, a[c]);
                    var f = egret.Tween.get(b);
                    f.to({
                            alpha: 1
                        },
                        200);
                    f.wait(2E3);
                    f.to({
                            alpha: 0
                        },
                        200);
                    f.call(h, this)
                };
            h()
        };
        b.prototype.changeDescription = function(a, b) {
            a.removeChildren();
            for (var c = 0,
                     e = 0; e < b.length; e++) {
                var h = b[e],
                    f = new egret.TextField;
                f.x = c;
                f.anchorX = f.anchorY = 0;
                f.textColor = parseInt(h.textColor);
                f.text = h.text;
                f.size = 40;
                a.addChild(f);
                c += f.width
            }
        };
        return b
    } (egret.DisplayObjectContainer);
Main.prototype.__class__ = "Main";
var setCacheInfoFunction = function() {
        if (PlatformFactory.getPlatform().name == PlatformTypes.WANBA) {
            var c = navigator.userAgent.toLowerCase(); - 1 < c.indexOf("qqbrowser") && ( - 1 < c.indexOf("gt-i9100 build") && (egret.Browser.useCacheAsBitmap = !1), -1 < c.indexOf("vivo x3t build") && (egret.Browser.useCacheAsBitmap = !1), -1 < c.indexOf("sm-g3818 build/jdq39") && (egret.Browser.useCacheAsBitmap = !1), -1 < c.indexOf("lenovo a630t build/imm76d") && (egret.Browser.useCacheAsBitmap = !1), -1 < c.indexOf("2013022 build/hm2013022") && (egret.Browser.useCacheAsBitmap = !1), -1 < c.indexOf("mi-one plus build") && (egret.Browser.useCacheAsBitmap = !1))
        }
    },
    CountdownModel = function() {
        function c(b) {
            this._data = b;
            Time.setInterval(this.countdown, this, 1E3)
        }
        c.prototype.getTime = function(b) {
            return this._data[b] ? this._data[b] : 0
        };
        c.prototype.over = function(b) {
            for (var a in this._data) this._data[a] = Math.max(0, this._data[a] - b),
                0 == b && delete this._data[a]
        };
        c.prototype.setTime = function(b, a) {
            this._data[b] = a
        };
        c.prototype.countdown = function() {
            for (var b in this._data) {
                var a = Math.max(0, this._data[b] - 1);
                this._data[b] = a;
                0 == a && delete this._data[b]
            }
        };
        c.prototype.getData = function() {
            return this._data
        };
        return c
    } ();
CountdownModel.prototype.__class__ = "CountdownModel";
var MainTickController = function() {
    function c() {
        this._tickNumber = this._timePiece = 0
    }
    c.getInstance = function() {
        null == this._instance && (this._instance = new c, this._instance.init());
        return this._instance
    };
    c.prototype.init = function() {
        addAction(Const.CHANGE_SPEED, this.changeSpeed, this)
    };
    c.prototype.changeSpeed = function(b) {
        Time.clearInterval(this._timeID);
        this._timeID = Time.setInterval(this.tick, this, 33 / b)
    };
    c.prototype.startTick = function() {
        this._timeID = Time.setInterval(this.tick, this, 33);
        Time.setInterval(this.saveData, this, 1E3)
    };
    c.prototype.saveData = function() {
        this._tickNumber += 1;
        this._tickNumber >= ProxyUtils.sfTime && (this._tickNumber = 0, DataCenter.getInstance().currentTime = (new Date).getTime(), DataCenter.getInstance().saveData());
        DataCenter.getInstance().resetRecordAttack();
        applyFilters(Const.SKILL_LAYER_EXSIT, !1) && DataCenter.getInstance().refreshCurrentDPS();
        DataCenter.getInstance().statistics.addTime(StatisticsType.onLineTime)
    };
    c.prototype.tick = function() {
        this._timePiece += 1;
        30 < this._timePiece && (this._timePiece = 0);
        for (var b = new NumberModel,
                 a = HeroController.getInstance().getPieceData(this._timePiece), d = applyFilters(Const.ISBOSS, !1), c = 0; c < a.length; c++) a[c].isDead() || (d ? b.add(a[c].bossDps) : b.add(a[c].dps));
        b = b.getNumer();
        "0" != b && doAction(Const.HERO_ATTACK, b)
    };
    return c
} ();
MainTickController.prototype.__class__ = "MainTickController";
var DataModel = function() {
    function c(b) {
        this.currentLife = this.holyNum = 0;
        this.currentPass = this.currentLevel = 1;
        this.heroList = {};
        this.mainHero = {};
        this.mainSkillCD = {};
        this.showHeroNum = 1;
        this.rebornSkillList = [];
        this.guideStep = 0;
        this.shareFalls = {};
        this.gameTime = 0;
        this.init(b)
    }
    c.prototype.init = function(b) {
        this.scNum = new NumberModel;
        this.scNum.add(decodeURIComponent(b.sc) || 0);
        this.gcNum = new NumberModel;
        this.gcNum.add(ProxyUtils.gcNum || 0);
        this.currentLife = b.curLife || 0;
        this.currentLevel = b.curLv || 1;
        this.currentPass = b.curPass || 1;
        this.heroList = b.hl || {};
        this.mainHero = b.mh || {
            lv: 1,
            skill: {
                22001 : 0,
                22002 : 0,
                22003 : 0,
                22004 : 0,
                22005 : 0,
                22006 : 0
            }
        };
        this.mainSkillCD = b.skillCD || {};
        this.showHeroNum = b.showNum || 1;
        this.rebornSkillList = b.rebornSkill || [];
        this.holyNum = b.holy || 0;
        this.guideStep = b.guideStep || 1;
        this.shareFalls = b.shareFalls || {};
        this.gameTime = b.gameTime || (new Date).getTime()
    };
    c.prototype.resetData = function() {
        this.scNum = new NumberModel;
        this.scNum.add(0);
        this.currentLife += 1;
        this.currentPass = this.currentLevel = 1;
        this.heroList = {};
        this.mainHero = {
            lv: 1,
            skill: {
                22001 : 0,
                22002 : 0,
                22003 : 0,
                22004 : 0,
                22005 : 0,
                22006 : 0
            }
        };
        this.mainSkillCD = {};
        this.showHeroNum = 1;
        this.guideStep = 7;
        this.shareFalls = {}
    };
    return c
} ();
DataModel.prototype.__class__ = "DataModel";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    BuyPayment = function(c) {
        function b() {
            c.apply(this, arguments)
        }
        __extends(b, c);
        b.prototype.effect = function(a, b, c) {
            void 0 === c && (c = null);
            PlatformFactory.getPlatform().payment(a, b, c)
        };
        return b
    } (BasePayment);
BuyPayment.prototype.__class__ = "BuyPayment";
PaymentFactory.getInstance().inject("buy", BuyPayment);
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    CoinPayment = function(c) {
        function b() {
            c.apply(this, arguments)
        }
        __extends(b, c);
        b.prototype.releaseSkill = function() {
            for (var a = 0; 5 > a; a++) {
                var b = new NumberModel;
                b.add(DataCenter.getInstance().getBossHP());
                b.times(2);
                b = new CoinElf(b.getNumer());
                applyFilters(Const.MAIN_LAYER).addChild(b);
                b.begin(GameUtils.random(100, 1E3))
            }
        };
        return b
    } (SkillPayment);
CoinPayment.prototype.__class__ = "CoinPayment";
PaymentFactory.getInstance().inject("coin", CoinPayment);
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    PaymentRecommend = function(c) {
        function b() {
            c.call(this);
            this.initView()
        }
        __extends(b, c);
        b.prototype.initView = function() {
            var a = ResourceUtils.createBitmapByName("mask");
            this.addChild(a);
            a.touchEnabled = !0;
            a.addEventListener(egret.TouchEvent.TOUCH_TAP, this.onMaskHandler, this);
            a = ResourceUtils.createBitmapByName("paymen_recommend");
            this.addChild(a);
            a.x = 24;
            a.y = 0;
            var a = new SimpleButton,
                b = ResourceUtils.createBitmapFromSheet("close_btn", "commonRes");
            a.addChild(b);
            a.addOnClick(this.onClickHandler, this);
            a.x = 555;
            a.y = 120;
            a.useZoomOut(!0);
            this.addChild(a);
            this._goldCoin = ResourceUtils.createBitmapFromSheet("gold_icon", "commonRes");
            this._goldCoin.x = 210;
            this._goldCoin.y = 175;
            this.addChild(this._goldCoin);
            this._notEnoughLabel = new egret.TextField;
            this._notEnoughLabel.text = "\u60a8\u7684\u94bb\u77f3\u4e0d\u8db3";
            this._notEnoughLabel.x = 260;
            this._notEnoughLabel.y = 175;
            this._notEnoughLabel.size = 28;
            this.addChild(this._notEnoughLabel);
            this._hotContainer = new SimpleButton;
            this._hotContainer.x = 325;
            this._hotContainer.y = 325;
            this._hotContainer.anchorX = this._hotContainer.anchorY = 0.5;
            this._hotContainer.useZoomOut(!0);
            this._hotContainer.addOnClick(this.payClickHandler, this);
            a = new egret.DisplayObjectContainer;
            a.cacheAsBitmap = !0;
            b = ResourceUtils.createBitmapFromSheet("payment_item_1", "commonRes");
            a.addChild(b);
            this._hotContainer.addChild(a);
            this._hotMoneyLabel = new egret.TextField;
            this._hotMoneyLabel.text = "180";
            this._hotMoneyLabel.x = 418;
            this._hotMoneyLabel.y = 14;
            this._hotMoneyLabel.size = 20;
            this._hotMoneyLabel.textAlign = egret.HorizontalAlign.CENTER;
            a.addChild(this._hotMoneyLabel);
            this._hotTitleLabel = new egret.TextField;
            this._hotTitleLabel.text = "\u597d\u70ed\u989d";
            this._hotTitleLabel.x = 150;
            this._hotTitleLabel.y = 12;
            this._hotTitleLabel.size = 28;
            a.addChild(this._hotTitleLabel);
            this._hotDescLabel = new egret.TextField;
            this._hotDescLabel.text = "\u70ed\u95e8\u7684\u5145\u503c\u989d";
            this._hotDescLabel.x = 150;
            this._hotDescLabel.y = 65;
            this._hotDescLabel.size = 21;
            this._hotDescLabel.lineSpacing = 5;
            a.addChild(this._hotDescLabel);
            this._hotIcon = ResourceUtils.createBitmapFromSheet("icon_payment_22", "iconRes");
            this._hotIcon.x = 15;
            this._hotIcon.y = 11;
            a.addChild(this._hotIcon);
            this.addChild(this._hotContainer);
            this._goodContainer = new SimpleButton;
            this._goodContainer.x = 325;
            this._goodContainer.y = 475;
            this._goodContainer.anchorX = this._goodContainer.anchorY = 0.5;
            this._goodContainer.useZoomOut(!0);
            this._goodContainer.addOnClick(this.payClickHandler, this);
            a = new egret.DisplayObjectContainer;
            a.cacheAsBitmap = !0;
            b = ResourceUtils.createBitmapFromSheet("payment_item_1", "commonRes");
            a.addChild(b);
            this._goodContainer.addChild(a);
            this._goodMoneyLabel = new egret.TextField;
            this._goodMoneyLabel.text = "180";
            this._goodMoneyLabel.x = 418;
            this._goodMoneyLabel.y = 14;
            this._goodMoneyLabel.size = 20;
            this._goodMoneyLabel.textAlign = egret.HorizontalAlign.CENTER;
            a.addChild(this._goodMoneyLabel);
            this._goodTitleLabel = new egret.TextField;
            this._goodTitleLabel.text = "\u8d85\u503c\u7684";
            this._goodTitleLabel.x = 150;
            this._goodTitleLabel.y = 12;
            this._goodTitleLabel.size = 28;
            a.addChild(this._goodTitleLabel);
            this._goodDescLabel = new egret.TextField;
            this._goodDescLabel.text = "\u5f88\u503c\u5f97\u5145\u503c\u989d";
            this._goodDescLabel.x = 150;
            this._goodDescLabel.y = 65;
            this._goodDescLabel.size = 21;
            this._goodDescLabel.lineSpacing = 5;
            a.addChild(this._goodDescLabel);
            this._goodIcon = ResourceUtils.createBitmapFromSheet("icon_payment_21", "iconRes");
            this._goodIcon.x = 15;
            this._goodIcon.y = 11;
            a.addChild(this._goodIcon);
            this.addChild(this._goodContainer);
            this.createNewTip(this._goodContainer, DataCenter.getInstance().getPaymentModel("good"), 25, 25);
            this.createNewTip(this._hotContainer, DataCenter.getInstance().getPaymentModel("hot"), 25, 25);
            this._goodContainer.addOnClick(this.payGood, this);
            this._hotContainer.addOnClick(this.payHot, this)
        };
        b.prototype.payGood = function() {
            this.payment(DataCenter.getInstance().getPaymentModel("good"))
        };
        b.prototype.payHot = function() {
            this.payment(DataCenter.getInstance().getPaymentModel("hot"))
        };
        b.prototype.payment = function(a) {
            PaymentFactory.getInstance().effect(a)
        };
        b.prototype.createNewTip = function(a, b, c, e) {
            b = ResourceUtils.createBitmapFromSheet(b.tip);
            b.anchorX = b._anchorY = 0.5;
            a.addChild(b);
            b.scaleX = b.scaleY = 0.7;
            b.x = c;
            b.y = e;
            egret.Tween.get(b, {
                loop: !0
            }).to({
                    scaleX: 0.9,
                    scaleY: 0.9
                },
                500).to({
                    scaleX: 0.7,
                    scaleY: 0.7
                },
                500)
        };
        b.prototype.refreshView = function() {
            var a = DataCenter.getInstance().getPaymentModel("good");
            this._goodMoneyLabel.text = a.getFormatValue();
            this._goodTitleLabel.text = a.title;
            this._goodDescLabel.textFlow = (new egret.HtmlTextParser).parser(a.getFormatDescription());
            a = DataCenter.getInstance().getPaymentModel("hot");
            this._hotMoneyLabel.text = a.getFormatValue();
            this._hotTitleLabel.text = a.title;
            this._hotDescLabel.textFlow = (new egret.HtmlTextParser).parser(a.getFormatDescription())
        };
        b.prototype.onMaskHandler = function() {};
        b.prototype.payClickHandler = function() {
            console.log("\u6211\u8981\u5145\u503c\u54a7")
        };
        b.prototype.onClickHandler = function() {
            GameMainLayer.instance.hidePaymentRecommendLayer()
        };
        return b
    } (BaseContainer);
PaymentRecommend.prototype.__class__ = "PaymentRecommend";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    UsePayment = function(c) {
        function b() {
            c.apply(this, arguments)
        }
        __extends(b, c);
        b.prototype.releaseSkill = function() {
            doAction(Const.USE_PAYMENT_SKILL, this._model.type)
        };
        return b
    } (SkillPayment);
UsePayment.prototype.__class__ = "UsePayment";
PaymentFactory.getInstance().inject("blood", UsePayment);
PaymentFactory.getInstance().inject("touch", UsePayment);
PaymentFactory.getInstance().inject("shield", UsePayment);
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    ETPlatform = function(c) {
        function b() {
            c.apply(this, arguments)
        }
        __extends(b, c);
        b.prototype.init = function() {
            EgretH5Sdk.checkLogin(this.logincb, this);
            addAction(Const.ACHIEVE, this.achieve, this);
            addAction(Const.SHOW_MAIN_LAYER, this.showMainLayer, this)
        };
        b.prototype.showMainLayer = function() {
            doAction(Const.START_GUIDE)
        };
        b.prototype.logincb = function(a) {
            a.token ? (ProxyUtils.requestUrl = "http://mezj.egret-labs.org/api.php", ProxyUtils.loginUrl = "http://mezj.egret-labs.org/login.php", ProxyUtils.platformInfo = platformType_egret, ProxyUtils.egretLogin(a.token)) : EgretH5Sdk.login(this.logincb, this)
        };
        b.prototype.achieve = function(a) {
            ProxyUtils.getGold(a.id)
        };
        b.prototype.deserialize = function() {
            return JSON.parse(ProxyUtils.serverData || "{}")
        };
        b.prototype._getPlatformName = function() {
            return PlatformTypes.EGRET
        };
        b.prototype.serialize = function(a) {
            ProxyUtils.saveData(JSON.stringify(a.serialize()))
        };
        b.prototype.getPlatformConfig = function() {
            return "payment_etConfig"
        };
        b.prototype.payment = function(a, b, c) {
            b = {};
            b.uId = ProxyUtils.platUserId;
            b.goodsId = a.id;
            b.goodsNumber = 1;
            b.serverId = 1;
            b.ext = "";
            b.appId = 46;
            EgretH5Sdk.pay(b)
        };
        return b
    } (BasePlatform);
ETPlatform.prototype.__class__ = "ETPlatform";
PlatformFactory.getInstance().inject("et", platformType_egret, ETPlatform);
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    WBPlatform = function(c) {
        function b() {
            c.apply(this, arguments)
        }
        __extends(b, c);
        b.prototype.init = function() {
            ProxyUtils.login();
            addAction(Const.ACHIEVE, this.achieve, this);
            addAction(Const.CLICK_CD_SKILL, this.clickCdSkill, this);
            addAction(Const.SHOW_MAIN_LAYER, this.showMainLayer, this)
        };
        b.prototype.showMainLayer = function() {
            doAction(Const.START_GUIDE)
        };
        b.prototype.clickCdSkill = function(a) {
            GameMainLayer.instance.showSkillCDLayer(a)
        };
        b.prototype.achieve = function(a) {
            ProxyUtils.getGold(a.id)
        };
        b.prototype.codeAward = function(a) {
            ProxyUtils.getCodeAward(a)
        };
        b.prototype.deserialize = function() {
            return JSON.parse(ProxyUtils.serverData || "{}")
        };
        b.prototype._getPlatformName = function() {
            return PlatformTypes.WANBA
        };
        b.prototype.serialize = function(a) {
            ProxyUtils.saveData(JSON.stringify(a.serialize()))
        };
        b.prototype.getPlatformConfig = function() {
            return "payment_wbConfig"
        };
        b.prototype.payment = function(a, b, c) {
            void 0 === c && (c = null);
            b = a.need;
            ProxyUtils.isVIP && (b *= 0.8);
            ProxyUtils.jifen >= b ? ProxyUtils.buyGold(parseInt(a.productId) + 1E3) : (egret.MainContext.instance.stage.touchEnabled = !1, egret.setTimeout(function() {
                    egret.MainContext.instance.stage.touchEnabled = !0
                },
                this, 1E3), WanBaApi.wanbarRecharge(ProxyUtils.isVIP, ProxyUtils.openkey, ProxyUtils.openid, ProxyUtils.appid, ProxyUtils.platform, ProxyUtils.jifen, b - ProxyUtils.jifen))
        };
        return b
    } (BasePlatform);
WBPlatform.prototype.__class__ = "WBPlatform";
PlatformFactory.getInstance().inject("wb", platformType_wanba, platformType_bearjoy, WBPlatform);
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    WXPlatform = function(c) {
        function b() {
            c.call(this);
            this._time = 1;
            this._shared = [];
            this._curtime = this._shareModel = null;
            this._share = [];
            this._award = this._shareAll = null;
            this._shareAll = new WxShareAll
        }
        __extends(b, c);
        b.prototype.init = function() {
            var a = GameUtils.getQuery("h", !0);
            egret.localStorage.setItem("hashKey", a);
            ProxyUtils.hashKey = a;
            Proxy.addGlobalParams("pf", GameUtils.getQuery("pf", !0));
            egret.localStorage.setItem("request_url", "http://h5.mezj.51.com/api.php");
            ProxyUtils.requestUrl = "http://h5.mezj.51.com/api.php";
            this._shareAll.shareQuery();
            this.getInfo();
            addAction(Const.ACHIEVE, this.achieve, this);
            addAction(Const.SHOW_MAIN_LAYER, this.showMainLayer, this);
            addAction(Const.SHOW_AD_DONE, this.showAdDone, this)
        };
        b.prototype.getPlatformConfig = function() {
            return "payment_wxConfig"
        };
        b.prototype._getPlatformName = function() {
            return PlatformTypes.WEIXIN
        };
        b.prototype.shareIsOpen = function() {
            return 1 == ProxyCache.getCache("User.getInfo").i.os
        };
        b.prototype.showAdDone = function(a) {
            WXUtils.showAward(a, !0, !0)
        };
        b.prototype.deserialize = function() {
            var a = ProxyCache.getCache("User.getInfo"),
                a = JSON.parse(a.i.dt || "{}");
            a.wx_share || (a.wx_share = {
                cdskill: 10,
                boxenemy: 1,
                nextlevel: 3
            });
            this._curtime = a.wx_time || (new Date).getTime();
            this._shareModel = new StatisticsModel(a.wx_share);
            return a
        };
        b.prototype.codeAward = function(a) {
            a = new SingleProxy({
                mod: "Code",
                "do": "exchange",
                p: {
                    code: a
                }
            });
            a.addEventListener(ProxyEvent.RESPONSE_SUCCEED,
                function(a) {
                    0 == a.responseData.s && (GameMainLayer.instance.hideCodeAwardLayer(), doAction(Const.DROP_COIN, {
                        type: 2,
                        num: 4,
                        one: "25"
                    }))
                },
                this);
            a.load();
            a.addEventListener(ProxyEvent.RESPONSE_ERROR,
                function(a) {
                    a = a.responseData;
                    3018 == a.s ? doAction(Const.SHOW_TIPS, "\u60a8\u5df2\u7ecf\u9886\u53d6\u8fc7\u4e86\u5151\u6362\u7801\u5956\u52b1") : 3019 == a.s ? doAction(Const.SHOW_TIPS, "\u8be5\u5151\u6362\u7801\u5df2\u7ecf\u4f7f\u7528\u8fc7\u4e86") : 3043 == a.s && doAction(Const.SHOW_TIPS, "\u60a8\u7684\u5151\u6362\u7801\u683c\u5f0f\u4e0d\u6b63\u786e")
                },
                this)
        };
        b.prototype.achieve = function(a) { (new SingleProxy({
            mod: "Achieve",
            "do": "finish",
            p: {
                aId: a.id
            }
        })).load()
        };
        b.prototype.getInfo = function() {
            var a = this,
                b = new SingleProxy({
                    mod: "User",
                    "do": "getInfo",
                    cache: !0
                });
            b.addEventListener(ProxyEvent.RESPONSE_SUCCEED,
                function(b) {
                    a._award = b.responseData.i;
                    ProxyUtils.awdTo = a._award.awdTo;
                    ProxyUtils.winxinUrl = a._award.wxurl;
                    ProxyUtils.boxUrl = a._award.boxurl;
                    b = a._award.gc;
                    ProxyUtils.sfTime = a._award.sf;
                    ProxyUtils.gcNum = b;
                    ProxyUtils.nickName = a._award.nNa;
                    ProxyUtils.userId = a._award.id;
                    doAction(Const.INFO_INITED)
                },
                this);
            b.load()
        };
        b.prototype.showMainLayer = function() {
            this._award && this._award.award ? (this._award.award.updated = !0, WXUtils.showAward(this._award.award, !1, !1,
                function() {
                    doAction(Const.START_GUIDE)
                })) : doAction(Const.START_GUIDE);
            this._award.skId && doAction(Const.CLEAN_SKILL_CD, this._award.skId);
            this.shareIsOpen() && (this._shared.push(new WXAwardBox), this._shared.push(new WXShareKillBox), this._shared.push(new WXShareNextLevel), this._shared.push(new WXShareUnlockSkill), ProxyUtils.awdTo && 1 == ProxyUtils.awdTo.show && doAction(Const.CHANGE_DEFAULT_SHARE, ProxyUtils.awdTo))
        };
        b.prototype.isNextDay = function(a, b) {
            var c = new Date(b);
            return (new Date(a)).getDate() != c.getDate()
        };
        b.prototype.getShare = function() {
            return this._shareModel
        };
        b.prototype.serializeDataCenter = function(a) {
            a = a.serialize();
            a.wx_share = this._shareModel.getData();
            a.wx_time = this._curtime;
            return a
        };
        b.prototype.serialize = function(a) {
            var b = this,
                c = (new Date).getTime();
            this.isNextDay(this._curtime, c) && this._shareModel.reset({
                cdskill: 10,
                boxenemy: 1,
                nextlevel: 3
            });
            this._curtime = c;
            a = JSON.stringify(this.serializeDataCenter(a));
            a = new SingleProxy({
                mod: "User",
                "do": "saveInfo",
                p: {
                    data: {
                        saveData: a,
                        time: this._time
                    }
                }
            });
            a.addEventListener(ProxyEvent.REQUEST_FAIL,
                function() {
                    ProxyUtils._errorTime += 1;
                    3 < ProxyUtils._errorTime && GameMainLayer.instance.showMaskLayer()
                },
                this);
            a.addEventListener(ProxyEvent.RESPONSE_SUCCEED,
                function(a) {
                    a = a.responseData;
                    a.skId && doAction(Const.CLEAN_SKILL_CD, a.skId);
                    a.award.updated = !0;
                    WXUtils.showAward(a.award, !1);
                    b._time++
                },
                this);
            a.load()
        };
        b.prototype.payment = function(a, b, c) {
            var e = this;
            void 0 === c && (c = null);
            b && b.call(c);
            a = new SingleProxy({
                mod: "User",
                "do": "getPayInfo",
                p: {
                    data: {
                        id: a.id
                    }
                }
            });
            a.addEventListener(ProxyEvent.RESPONSE_SUCCEED,
                function(a) {
                    wx.ready(function() {
                        var b = a.responseData;
                        wx.chooseWXPay({
                            timestamp: b.timestamp,
                            nonceStr: b.nonceStr,
                            package: b["package"],
                            signType: b.signType,
                            paySign: b.paySign,
                            success: function() {
                                var a = new SingleProxy({
                                    mod: "User",
                                    "do": "payResult",
                                    p: {
                                        data: {
                                            orderId: b.orderId
                                        }
                                    }
                                });
                                a.addEventListener(ProxyEvent.RESPONSE_SUCCEED,
                                    function(a) {
                                        doAction(Const.AWARD_EFFECT, {
                                            type: "diamond",
                                            award: a.responseData.diamond,
                                            updated: !0
                                        })
                                    },
                                    e);
                                a.load()
                            }
                        })
                    })
                },
                this);
            a.load()
        };
        return b
    } (BasePlatform);
WXPlatform.prototype.__class__ = "WXPlatform";
PlatformFactory.getInstance().inject("wx", WXPlatform);
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    GuideButtonLayer = function(c) {
        function b() {
            c.call(this);
            this._left = !1
        }
        __extends(b, c);
        b.prototype.dispose = function() {
            c.prototype.dispose.call(this);
            this._context = this._selector = this._button = null;
            this._timerId && Time.clearInterval(this._timerId);
            this._hand && egret.Tween.removeTweens(this._hand);
            this._circle && egret.Tween.removeTweens(this._circle)
        };
        b.prototype.updateView = function(a, b, c, e) {
            void 0 === b && (b = null);
            void 0 === c && (c = null);
            void 0 === e && (e = null);
            this._circle = this._mask = this._hand = null;
            this._button = a;
            this._data = b;
            this._selector = c;
            this._context = e
        };
        b.prototype.onClickedButton = function(a) {
            console.log("hehe")
        };
        b.prototype._onAddToStage = function() {
            c.prototype._onAddToStage.call(this);
            addAction("changeMaskHeight", this.changeMask, this)
        };
        b.prototype.changeMask = function() {
            null == this._mask && (this._mask = new Mask(ResourceUtils.getTextureFromSheet("guide_mask", "commonRes")), this._mask.width = GameUtils.getWinWidth(), this._mask.height = GameUtils.getWinHeight(), this.addChild(this._mask));
            var a = this._button.localToGlobal(0, 0),
                a = this.globalToLocal(a.x, a.y);
            this._mask.tempHeight = 550;
            this._mask.show(a.x, a.y, this._button.width, this._button.height)
        };
        b.prototype.maskAndHand = function() {
            this.createMask();
            this.createGuideHand()
        };
        b.prototype.createMask = function() {
            null == this._mask && (this._mask = new Mask(ResourceUtils.getTextureFromSheet("guide_mask", "commonRes")), this._mask.width = GameUtils.getWinWidth(), this._mask.height = GameUtils.getWinHeight(), this.addChild(this._mask));
            var a = this._button.localToGlobal(0, 0),
                a = this.globalToLocal(a.x, a.y);
            this._mask.show(a.x, a.y, this._button.width, this._button.height)
        };
        b.prototype.createGuideHand = function() {
            var a = this._button.localToGlobal(0, 0),
                a = this.globalToLocal(a.x, a.y);
            if (null == this._circle) {
                this._circle = ResourceUtils.createBitmapFromSheet("guide_circle", "commonRes");
                this._circle.anchorX = 0.5;
                this._circle.anchorY = 0.5;
                this._circle.alpha = 1;
                this._circle.scaleX = this._circle.scaleY = 0.75;
                this.addChild(this._circle);
                var b = egret.Tween.get(this._circle, {
                    loop: !0
                });
                b.to({
                        scaleX: 1,
                        scaleY: 1,
                        alpha: 0.5
                    },
                    500);
                b.wait(10);
                b.to({
                        scaleX: 0.75,
                        scaleY: 0.75,
                        alpha: 1
                    },
                    500);
                b.wait(10)
            }
            null == this._hand && (750 < a.x ? (this._left = !0, this._hand = ResourceUtils.createBitmapFromSheet("guide_hand1", "commonRes")) : (this._left = !1, this._hand = ResourceUtils.createBitmapFromSheet("guide_hand", "commonRes")), this._hand.anchorX = 0.5, this._hand.anchorY = 0.5, this.addChild(this._hand), b = egret.Tween.get(this._hand, {
                loop: !0
            }), b.to({
                    scaleX: 0.75,
                    scaleY: 0.75
                },
                500), b.wait(10), b.to({
                    scaleX: 1,
                    scaleY: 1
                },
                500), b.wait(10));
            var c, b = this._button.scaleX;
            c = this._button.scaleY;
            this._left ? (this._hand.x = a.x + this._button.width * b * 0.5 - 24, this._hand.y = a.y + this._button.height * c * 0.5 + 29, this._circle.x = this._hand.x + 25) : (this._hand.x = a.x + this._button.width * b * 0.5 + 25.5, this._hand.y = a.y + this._button.height * c * 0.5 + 28.5, this._circle.x = this._hand.x - 25);
            this._circle.y = this._hand.y - 28
        };
        return b
    } (BaseContainer);
GuideButtonLayer.prototype.__class__ = "GuideButtonLayer";
var GuideManager = function() {
    function c() {}
    c.prototype.execute = function(b) {
        PlatformFactory.getPlatform().name != PlatformTypes.WEIXIN && 5 == b && (b = 6);
        var a = {
            1 : Guide1,
            2 : Guide2,
            3 : Guide3,
            4 : Guide4,
            5 : Guide5,
            6 : Guide6
        };
        a[b] && (new a[b]).execute()
    };
    return c
} ();
GuideManager.prototype.__class__ = "GuideManager";
var Guide1 = function() {
    function c() {}
    c.prototype.execute = function() {
        doAction(Guide.HIDE_BOTTOM);
        GameMainLayer.instance.showGuide(Guide.handPositions.hitBoss.x, Guide.handPositions.hitBoss.y);
        addAction(Guide.CLICK_HOME, this.dispose, this)
    };
    c.prototype.dispose = function() {
        removeEventsByTarget(this);
        GameMainLayer.instance.hideGuide(); (new Guide2).execute();
        DataCenter.getInstance().totalData.guideStep = 2;
        DataCenter.getInstance().saveData()
    };
    return c
} ();
Guide1.prototype.__class__ = "Guide1";
var Guide2 = function() {
    function c() {}
    c.prototype.execute = function() {
        doAction(Guide.HIDE_BOTTOM);
        DataCenter.getInstance().hasEnoughSc(5) ? (doAction(Guide.SHOW_BOTTOM1), addAction("mainButtonClick", this.commandContinue, this), GameMainLayer.instance.showGuide(Guide.handPositions.clickMain.x, Guide.handPositions.clickMain.y)) : addAction(Const.CHANGE_COIN, this.changeHandler, this)
    };
    c.prototype.changeHandler = function() {
        removeEventsByTarget(this);
        this.execute()
    };
    c.prototype.commandContinue = function() {
        doAction("changeMaskHeight");
        removeEventsByTarget(this);
        addAction(Guide.UP_HERO, this.dispose, this);
        GameMainLayer.instance.showGuide(Guide.handPositions.upSkill.x, Guide.handPositions.upSkill.y)
    };
    c.prototype.dispose = function() {
        removeEventsByTarget(this);
        GameMainLayer.instance.hideGuide(); (new Guide3).execute();
        DataCenter.getInstance().totalData.guideStep = 3;
        DataCenter.getInstance().saveData()
    };
    return c
} ();
Guide2.prototype.__class__ = "Guide2";
var Guide3 = function() {
    function c() {}
    c.prototype.execute = function() {
        DataCenter.getInstance().hasEnoughSc(50) ? applyFilters(Guide.IS_SKILL_LAYER_EXSIT) || "hero" != applyFilters("get_current_tag") ? (addAction("heroButtonClick", this.commandContinue, this), GameMainLayer.instance.showGuide(Guide.handPositions.bottom2.x, Guide.handPositions.bottom2.y), doAction("changeMaskHeight")) : this.commandContinue() : addAction(Const.CHANGE_COIN, this.changeHandler, this)
    };
    c.prototype.changeHandler = function() {
        removeEventsByTarget(this);
        this.execute()
    };
    c.prototype.commandContinue = function() {
        removeEventsByTarget(this);
        addAction(Guide.BUY_HERO, this.dispose, this);
        GameMainLayer.instance.showGuide(Guide.handPositions.buyHero.x, Guide.handPositions.buyHero.y);
        doAction("changeMaskHeight")
    };
    c.prototype.dispose = function() {
        removeEventsByTarget(this);
        GameMainLayer.instance.hideGuide();
        doAction(Guide.SHOW_BOTTOM2); (new Guide4).execute();
        DataCenter.getInstance().totalData.guideStep = 4;
        DataCenter.getInstance().saveData()
    };
    return c
} ();
Guide3.prototype.__class__ = "Guide3";
var Guide4 = function() {
    function c() {
        this._step = 1
    }
    c.prototype.execute = function() {
        50 <= HeroController.getInstance().mainHero.lv ? 0 == HeroController.getInstance().mainHero.skillList[0].lv ? DataCenter.getInstance().hasEnoughSc("2.78K") ? applyFilters(Guide.IS_SKILL_LAYER_EXSIT) || "main" != applyFilters("get_current_tag") ? (addAction("mainButtonClick", this.commandContinue, this), GameMainLayer.instance.showGuide(Guide.handPositions.clickMain.x, Guide.handPositions.clickMain.y), doAction("changeMaskHeight")) : this.commandContinue() : addAction(Const.CHANGE_COIN, this.changeHandler, this) : (this._step = 2, this.commandContinue()) : addAction(Const.CHANGE_COIN, this.changeHandler, this)
    };
    c.prototype.changeHandler = function() {
        removeEventsByTarget(this);
        this.execute()
    };
    c.prototype.commandContinue = function() {
        removeEventsByTarget(this);
        this._step += 1;
        2 == this._step ? (addAction(Guide.UNLOCK_MAIN_SKILL, this.commandContinue, this), GameMainLayer.instance.showGuide(Guide.handPositions.buyMainSkill.x, Guide.handPositions.buyMainSkill.y)) : 3 == this._step ? (addAction(Guide.CLICK_DOWN_JIANTOU, this.commandContinue, this), GameMainLayer.instance.showGuide(Guide.handPositions.clickSanjiao.x, Guide.handPositions.clickSanjiao.y)) : 4 == this._step && (addAction(Guide.USE_SKILL, this.dispose, this), GameMainLayer.instance.showGuide(Guide.handPositions.useSkill.x, Guide.handPositions.useSkill.y));
        doAction("changeMaskHeight")
    };
    c.prototype.dispose = function() {
        removeEventsByTarget(this);
        GameMainLayer.instance.hideGuide();
        PlatformFactory.getPlatform().name == PlatformTypes.WEIXIN ? ((new Guide5).execute(), DataCenter.getInstance().totalData.guideStep = 5) : ((new Guide6).execute(), DataCenter.getInstance().totalData.guideStep = 6);
        DataCenter.getInstance().saveData()
    };
    return c
} ();
Guide4.prototype.__class__ = "Guide4";
var Guide5 = function() {
    function c() {}
    c.prototype.execute = function() {
        var b = DataCenter.getInstance().totalData.mainSkillCD["22001"];
        6E5 < (new Date).getTime() - b || !b ? ((new Guide6).execute(), DataCenter.getInstance().totalData.guideStep = 6, DataCenter.getInstance().saveData()) : !1 == applyFilters(Guide.IS_SKILL_LAYER_EXSIT) ? (addAction(Guide.CLICK_DOWN_JIANTOU, this.commandContinue, this), GameMainLayer.instance.showGuide(Guide.handPositions.clickSanjiao.x, Guide.handPositions.clickSanjiao.y)) : 22001 == applyFilters(Guide.IS_SKILL_CDING, 22001) ? Time.setTimeout(this.execute, this, 1E3) : (addAction(Const.CLICK_CD_SKILL, this.dispose, this), GameMainLayer.instance.showGuide(Guide.handPositions.useSkill.x, Guide.handPositions.useSkill.y))
    };
    c.prototype.commandContinue = function() {
        removeEventsByTarget(this);
        this.execute()
    };
    c.prototype.dispose = function() {
        removeEventsByTarget(this);
        GameMainLayer.instance.hideGuide(); (new Guide6).execute();
        DataCenter.getInstance().totalData.guideStep = 6;
        DataCenter.getInstance().saveData()
    };
    return c
} ();
Guide5.prototype.__class__ = "Guide5";
var Guide6 = function() {
    function c() {}
    c.prototype.execute = function() {
        81 <= DataCenter.getInstance().totalData.currentLevel ? applyFilters(Guide.IS_SKILL_LAYER_EXSIT) || "holy" != applyFilters("get_current_tag") ? (addAction("holyButtonClick", this.commandContinue, this), GameMainLayer.instance.showGuide(Guide.handPositions.bottom3.x, Guide.handPositions.bottom3.y), doAction("changeMaskHeight")) : this.commandContinue() : addAction(Const.BOSS_KILLED, this.changeHandler, this)
    };
    c.prototype.commandContinue = function() {
        removeEventsByTarget(this);
        addAction(Guide.BUY_HOLY, this.dispose, this);
        GameMainLayer.instance.showGuide(Guide.handPositions.buyHero.x, Guide.handPositions.buyHero.y);
        doAction("changeMaskHeight")
    };
    c.prototype.changeHandler = function() {
        removeEventsByTarget(this);
        this.execute()
    };
    c.prototype.dispose = function() {
        removeEventsByTarget(this);
        GameMainLayer.instance.hideGuide();
        DataCenter.getInstance().totalData.guideStep = 7;
        DataCenter.getInstance().saveData()
    };
    return c
} ();
Guide6.prototype.__class__ = "Guide6";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    BezierAnimation = function(c) {
        function b(a, b, g) {
            void 0 === b && (b = !0);
            void 0 === g && (g = null);
            c.call(this, a, g);
            this._points = [];
            this._canRotate = b
        }
        __extends(b, c);
        b.prototype.appendPoint = function(a) {
            4 > this._points.length && this._points.push(a);
            return this
        };
        b.prototype.appendXY = function(a, b) {
            4 > this._points.length && this._points.push(new egret.Point(a, b));
            return this
        };
        b.prototype.update = function(a) {
            if (4 == this._points.length) {
                var d = Math.pow(1 - a, 3) * this._points[0].x + 3 * this._points[1].x * a * (1 - a) * (1 - a) + 3 * this._points[2].x * a * a * (1 - a) + this._points[3].x * Math.pow(a, 3),
                    g = Math.pow(1 - a, 3) * this._points[0].y + 3 * this._points[1].y * a * (1 - a) * (1 - a) + 3 * this._points[2].y * a * a * (1 - a) + this._points[3].y * Math.pow(a, 3);
                this._target.x = d;
                this._target.y = g;
                this._canRotate && (this._target.rotation = b.getDegress(a, this._points[0], this._points[1], this._points[2], this._points[3]))
            }
            c.prototype.update.call(this, a)
        };
        b.prototype.onComplete = function() {
            c.prototype.onComplete.call(this)
        };
        b.prototype.run = function(a) {
            c.prototype._run.call(this, a)
        };
        b.getDegress = function(a, b, c, e, h) {
            b = new egret.Point((1 - a) * b.x + a * c.x, (1 - a) * b.y + a * c.y);
            c = new egret.Point((1 - a) * c.x + a * e.x, (1 - a) * c.y + a * e.y);
            e = new egret.Point((1 - a) * e.x + a * h.x, (1 - a) * e.y + a * h.y);
            h = new egret.Point((1 - a) * b.x + a * c.x, (1 - a) * b.y + a * c.y);
            a = new egret.Point((1 - a) * c.x + a * e.x, (1 - a) * c.y + a * e.y);
            return 180 * Math.atan2( - (h.y - a.y), a.x - h.x) / Math.PI
        };
        return b
    } (GameAnimation);
BezierAnimation.prototype.__class__ = "BezierAnimation";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    ScoreAnimation = function(c) {
        function b(a, b, g) {
            c.call(this, a);
            this._format = null;
            this._beginScore = b;
            this._endScore = g;
            this._mns = new NumberModel
        }
        __extends(b, c);
        b.prototype.update = function(a) {
            c.prototype.update.call(this, a)
        };
        b.prototype.run = function(a) {
            c.prototype._run.call(this, a)
        };
        return b
    } (GameAnimation);
ScoreAnimation.prototype.__class__ = "ScoreAnimation";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    MovieEvent = function(c) {
        function b(a, b, g, e) {
            void 0 === b && (b = null);
            void 0 === g && (g = !1);
            void 0 === e && (e = !1);
            c.call(this, a, g, e);
            this._events = null;
            null != b && (this._events = b.split("-"))
        }
        __extends(b, c);
        Object.defineProperty(b.prototype, "frameEventType", {
            get: function() {
                return null != this._events && 2 <= this._events.length ? this._events[0] : null
            },
            enumerable: !0,
            configurable: !0
        });
        Object.defineProperty(b.prototype, "frameEventLabel", {
            get: function() {
                return this._events && 2 <= this._events.length ? this._events[1] : this._events && 1 == this._events.length ? this._events[0] : null
            },
            enumerable: !0,
            configurable: !0
        });
        b.PLAY_COMPLETE = "playComplete";
        b.FRAME_EVENT = "frameEvent";
        b.ALL = "all";
        return b
    } (egret.Event);
MovieEvent.prototype.__class__ = "MovieEvent";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    MultiProxy = function(c) {
        function b() {
            for (var a = [], b = 0; b < arguments.length; b++) a[b - 0] = arguments[b];
            c.call(this, null);
            this._subProxys = [];
            b = [];
            if (1 == a.length && Array.isArray(a[0])) b = a[0];
            else if (0 < a.length) for (var g = 0; g < a.length; g++) b.push(a[g]);
            if (b) for (g = 0; g < b.length; g++) this.addSubProxy(b[g]);
            this.addParam("m", 1)
        }
        __extends(b, c);
        b.prototype.getParamString = function() {
            var a = [];
            this._subProxys.forEach(function(b) {
                    a.push(b.params)
                },
                this);
            return JSON.stringify(a)
        };
        b.prototype.onResponse = function(a) {
            var b = this;
            a ? (this._responseData = a, this._subProxys.forEach(function(a) {
                    var c = a.getParamByName("mod"),
                        h = a.getParamByName("do");
                    a.onResponse(b.responseData[c][h])
                },
                this), this._isResponseSucceed = this._isRequestSucceed = !0, doAction("multiproxy_response_succeed", this), this.dispatchEvent(new ProxyEvent(ProxyEvent.RESPONSE_SUCCEED, this)), this.dispatchEvent(new ProxyEvent(ProxyEvent.REQUEST_SUCCEED, this))) : (this._isRequestSucceed = !0, this._isResponseSucceed = !1, this._errorCode = -1E3, doAction("multiproxy_response_error", this), this.dispatchEvent(new ProxyEvent(ProxyEvent.RESPONSE_ERROR, this)), this.dispatchEvent(new ProxyEvent(ProxyEvent.REQUEST_SUCCEED, this)), this.dispatchEvent(new ProxyEvent(ProxyEvent.ERROR, this)))
        };
        Object.defineProperty(b.prototype, "subProxyList", {
            get: function() {
                return this._subProxys
            },
            enumerable: !0,
            configurable: !0
        });
        b.prototype.getParamByName = function(a) {
            for (var b = 0; b < this._subProxys.length; b++) {
                var c = this._subProxys[b].getParamByName(a);
                if (null != c) return c
            }
            return null
        };
        b.prototype.addSubProxy = function(a) {
            if (a && a.hasOwnProperty("mod")) this._subProxys.push(new SingleProxy(a));
            else if (a instanceof SingleProxy) this._subProxys.push(a);
            else if (a instanceof b) {
                var c = this;
                a._subProxys.forEach(function(a) {
                        c.addSubProxy(a)
                    },
                    this)
            }
        };
        return b
    } (Proxy);
MultiProxy.prototype.__class__ = "MultiProxy";
var ProxyStatus = function() {
    function c() {}
    c.DEFAULT = 1;
    c.REQUEST = 2;
    c.WAIT = 4;
    c.RESPONSE = 8;
    return c
} ();
ProxyStatus.prototype.__class__ = "ProxyStatus";
var RequestStatus = function() {
    function c() {}
    c.SUCCEED = 1;
    c.ERROR = 2;
    return c
} ();
RequestStatus.prototype.__class__ = "RequestStatus";
var md5 = function() {
    function c() {
        this.hexcase = 0;
        this.b64pad = ""
    }
    c.prototype.hex_md5 = function(b) {
        return this.rstr2hex(this.rstr_md5(this.str2rstr_utf8(b)))
    };
    c.prototype.b64_md5 = function(b) {
        return this.rstr2b64(this.rstr_md5(this.str2rstr_utf8(b)))
    };
    c.prototype.any_md5 = function(b, a) {
        return this.rstr2any(this.rstr_md5(this.str2rstr_utf8(b)), a)
    };
    c.prototype.hex_hmac_md5 = function(b, a) {
        return this.rstr2hex(this.rstr_hmac_md5(this.str2rstr_utf8(b), this.str2rstr_utf8(a)))
    };
    c.prototype.b64_hmac_md5 = function(b, a) {
        return this.rstr2b64(this.rstr_hmac_md5(this.str2rstr_utf8(b), this.str2rstr_utf8(a)))
    };
    c.prototype.any_hmac_md5 = function(b, a, c) {
        return this.rstr2any(this.rstr_hmac_md5(this.str2rstr_utf8(b), this.str2rstr_utf8(a)), c)
    };
    c.prototype.md5_vm_test = function() {
        return "900150983cd24fb0d6963f7d28e17f72" == this.hex_md5("abc").toLowerCase()
    };
    c.prototype.rstr_md5 = function(b) {
        return this.binl2rstr(this.binl_md5(this.rstr2binl(b), 8 * b.length))
    };
    c.prototype.rstr_hmac_md5 = function(b, a) {
        var c = this.rstr2binl(b);
        16 < c.length && (c = this.binl_md5(c, 8 * b.length));
        for (var g = Array(16), e = Array(16), h = 0; 16 > h; h++) g[h] = c[h] ^ 909522486,
            e[h] = c[h] ^ 1549556828;
        c = this.binl_md5(g.concat(this.rstr2binl(a)), 512 + 8 * a.length);
        return this.binl2rstr(this.binl_md5(e.concat(c), 640))
    };
    c.prototype.rstr2hex = function(b) {
        try {
            this.hexcase
        } catch(a) {
            this.hexcase = 0
        }
        for (var c = this.hexcase ? "0123456789ABCDEF": "0123456789abcdef", g = "", e, h = 0; h < b.length; h++) e = b.charCodeAt(h),
            g += c.charAt(e >>> 4 & 15) + c.charAt(e & 15);
        return g
    };
    c.prototype.rstr2b64 = function(b) {
        try {
            this.b64pad
        } catch(a) {
            this.b64pad = ""
        }
        for (var c = "",
                 g = b.length,
                 e = 0; e < g; e += 3) for (var h = b.charCodeAt(e) << 16 | (e + 1 < g ? b.charCodeAt(e + 1) << 8 : 0) | (e + 2 < g ? b.charCodeAt(e + 2) : 0), f = 0; 4 > f; f++) c = 8 * e + 6 * f > 8 * b.length ? c + this.b64pad: c + "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(h >>> 6 * (3 - f) & 63);
        return c
    };
    c.prototype.rstr2any = function(b, a) {
        var c = a.length,
            g, e, h, f, k, l = Array(Math.ceil(b.length / 2));
        for (g = 0; g < l.length; g++) l[g] = b.charCodeAt(2 * g) << 8 | b.charCodeAt(2 * g + 1);
        var m = Math.ceil(8 * b.length / (Math.log(a.length) / Math.log(2))),
            p = Array(m);
        for (e = 0; e < m; e++) {
            k = [];
            for (g = f = 0; g < l.length; g++) if (f = (f << 16) + l[g], h = Math.floor(f / c), f -= h * c, 0 < k.length || 0 < h) k[k.length] = h;
            p[e] = f;
            l = k
        }
        c = "";
        for (g = p.length - 1; 0 <= g; g--) c += a.charAt(p[g]);
        return c
    };
    c.prototype.str2rstr_utf8 = function(b) {
        for (var a = "",
                 c = -1,
                 g, e; ++c < b.length;) g = b.charCodeAt(c),
            e = c + 1 < b.length ? b.charCodeAt(c + 1) : 0,
            55296 <= g && 56319 >= g && 56320 <= e && 57343 >= e && (g = 65536 + ((g & 1023) << 10) + (e & 1023), c++),
                127 >= g ? a += String.fromCharCode(g) : 2047 >= g ? a += String.fromCharCode(192 | g >>> 6 & 31, 128 | g & 63) : 65535 >= g ? a += String.fromCharCode(224 | g >>> 12 & 15, 128 | g >>> 6 & 63, 128 | g & 63) : 2097151 >= g && (a += String.fromCharCode(240 | g >>> 18 & 7, 128 | g >>> 12 & 63, 128 | g >>> 6 & 63, 128 | g & 63));
        return a
    };
    c.prototype.str2rstr_utf16le = function(b) {
        for (var a = "",
                 c = 0; c < b.length; c++) a += String.fromCharCode(b.charCodeAt(c) & 255, b.charCodeAt(c) >>> 8 & 255);
        return a
    };
    c.prototype.str2rstr_utf16be = function(b) {
        for (var a = "",
                 c = 0; c < b.length; c++) a += String.fromCharCode(b.charCodeAt(c) >>> 8 & 255, b.charCodeAt(c) & 255);
        return a
    };
    c.prototype.rstr2binl = function(b) {
        for (var a = Array(b.length >> 2), c = 0; c < a.length; c++) a[c] = 0;
        for (c = 0; c < 8 * b.length; c += 8) a[c >> 5] |= (b.charCodeAt(c / 8) & 255) << c % 32;
        return a
    };
    c.prototype.binl2rstr = function(b) {
        for (var a = "",
                 c = 0; c < 32 * b.length; c += 8) a += String.fromCharCode(b[c >> 5] >>> c % 32 & 255);
        return a
    };
    c.prototype.binl_md5 = function(b, a) {
        b[a >> 5] |= 128 << a % 32;
        b[(a + 64 >>> 9 << 4) + 14] = a;
        for (var c = 1732584193,
                 g = -271733879,
                 e = -1732584194,
                 h = 271733878,
                 f = 0; f < b.length; f += 16) var k = c,
                 l = g,
                 m = e,
                 p = h,
                 c = this.md5_ff(c, g, e, h, b[f + 0], 7, -680876936),
                 h = this.md5_ff(h, c, g, e, b[f + 1], 12, -389564586),
                 e = this.md5_ff(e, h, c, g, b[f + 2], 17, 606105819),
                 g = this.md5_ff(g, e, h, c, b[f + 3], 22, -1044525330),
                 c = this.md5_ff(c, g, e, h, b[f + 4], 7, -176418897),
                 h = this.md5_ff(h, c, g, e, b[f + 5], 12, 1200080426),
                 e = this.md5_ff(e, h, c, g, b[f + 6], 17, -1473231341),
                 g = this.md5_ff(g, e, h, c, b[f + 7], 22, -45705983),
                 c = this.md5_ff(c, g, e, h, b[f + 8], 7, 1770035416),
                 h = this.md5_ff(h, c, g, e, b[f + 9], 12, -1958414417),
                 e = this.md5_ff(e, h, c, g, b[f + 10], 17, -42063),
                 g = this.md5_ff(g, e, h, c, b[f + 11], 22, -1990404162),
                 c = this.md5_ff(c, g, e, h, b[f + 12], 7, 1804603682),
                 h = this.md5_ff(h, c, g, e, b[f + 13], 12, -40341101),
                 e = this.md5_ff(e, h, c, g, b[f + 14], 17, -1502002290),
                 g = this.md5_ff(g, e, h, c, b[f + 15], 22, 1236535329),
                 c = this.md5_gg(c, g, e, h, b[f + 1], 5, -165796510),
                 h = this.md5_gg(h, c, g, e, b[f + 6], 9, -1069501632),
                 e = this.md5_gg(e, h, c, g, b[f + 11], 14, 643717713),
                 g = this.md5_gg(g, e, h, c, b[f + 0], 20, -373897302),
                 c = this.md5_gg(c, g, e, h, b[f + 5], 5, -701558691),
                 h = this.md5_gg(h, c, g, e, b[f + 10], 9, 38016083),
                 e = this.md5_gg(e, h, c, g, b[f + 15], 14, -660478335),
                 g = this.md5_gg(g, e, h, c, b[f + 4], 20, -405537848),
                 c = this.md5_gg(c, g, e, h, b[f + 9], 5, 568446438),
                 h = this.md5_gg(h, c, g, e, b[f + 14], 9, -1019803690),
                 e = this.md5_gg(e, h, c, g, b[f + 3], 14, -187363961),
                 g = this.md5_gg(g, e, h, c, b[f + 8], 20, 1163531501),
                 c = this.md5_gg(c, g, e, h, b[f + 13], 5, -1444681467),
                 h = this.md5_gg(h, c, g, e, b[f + 2], 9, -51403784),
                 e = this.md5_gg(e, h, c, g, b[f + 7], 14, 1735328473),
                 g = this.md5_gg(g, e, h, c, b[f + 12], 20, -1926607734),
                 c = this.md5_hh(c, g, e, h, b[f + 5], 4, -378558),
                 h = this.md5_hh(h, c, g, e, b[f + 8], 11, -2022574463),
                 e = this.md5_hh(e, h, c, g, b[f + 11], 16, 1839030562),
                 g = this.md5_hh(g, e, h, c, b[f + 14], 23, -35309556),
                 c = this.md5_hh(c, g, e, h, b[f + 1], 4, -1530992060),
                 h = this.md5_hh(h, c, g, e, b[f + 4], 11, 1272893353),
                 e = this.md5_hh(e, h, c, g, b[f + 7], 16, -155497632),
                 g = this.md5_hh(g, e, h, c, b[f + 10], 23, -1094730640),
                 c = this.md5_hh(c, g, e, h, b[f + 13], 4, 681279174),
                 h = this.md5_hh(h, c, g, e, b[f + 0], 11, -358537222),
                 e = this.md5_hh(e, h, c, g, b[f + 3], 16, -722521979),
                 g = this.md5_hh(g, e, h, c, b[f + 6], 23, 76029189),
                 c = this.md5_hh(c, g, e, h, b[f + 9], 4, -640364487),
                 h = this.md5_hh(h, c, g, e, b[f + 12], 11, -421815835),
                 e = this.md5_hh(e, h, c, g, b[f + 15], 16, 530742520),
                 g = this.md5_hh(g, e, h, c, b[f + 2], 23, -995338651),
                 c = this.md5_ii(c, g, e, h, b[f + 0], 6, -198630844),
                 h = this.md5_ii(h, c, g, e, b[f + 7], 10, 1126891415),
                 e = this.md5_ii(e, h, c, g, b[f + 14], 15, -1416354905),
                 g = this.md5_ii(g, e, h, c, b[f + 5], 21, -57434055),
                 c = this.md5_ii(c, g, e, h, b[f + 12], 6, 1700485571),
                 h = this.md5_ii(h, c, g, e, b[f + 3], 10, -1894986606),
                 e = this.md5_ii(e, h, c, g, b[f + 10], 15, -1051523),
                 g = this.md5_ii(g, e, h, c, b[f + 1], 21, -2054922799),
                 c = this.md5_ii(c, g, e, h, b[f + 8], 6, 1873313359),
                 h = this.md5_ii(h, c, g, e, b[f + 15], 10, -30611744),
                 e = this.md5_ii(e, h, c, g, b[f + 6], 15, -1560198380),
                 g = this.md5_ii(g, e, h, c, b[f + 13], 21, 1309151649),
                 c = this.md5_ii(c, g, e, h, b[f + 4], 6, -145523070),
                 h = this.md5_ii(h, c, g, e, b[f + 11], 10, -1120210379),
                 e = this.md5_ii(e, h, c, g, b[f + 2], 15, 718787259),
                 g = this.md5_ii(g, e, h, c, b[f + 9], 21, -343485551),
                 c = this.safe_add(c, k),
                 g = this.safe_add(g, l),
                 e = this.safe_add(e, m),
                 h = this.safe_add(h, p);
        return [c, g, e, h]
    };
    c.prototype.md5_cmn = function(b, a, c, g, e, h) {
        return this.safe_add(this.bit_rol(this.safe_add(this.safe_add(a, b), this.safe_add(g, h)), e), c)
    };
    c.prototype.md5_ff = function(b, a, c, g, e, h, f) {
        return this.md5_cmn(a & c | ~a & g, b, a, e, h, f)
    };
    c.prototype.md5_gg = function(b, a, c, g, e, h, f) {
        return this.md5_cmn(a & g | c & ~g, b, a, e, h, f)
    };
    c.prototype.md5_hh = function(b, a, c, g, e, h, f) {
        return this.md5_cmn(a ^ c ^ g, b, a, e, h, f)
    };
    c.prototype.md5_ii = function(b, a, c, g, e, h, f) {
        return this.md5_cmn(c ^ (a | ~g), b, a, e, h, f)
    };
    c.prototype.safe_add = function(b, a) {
        var c = (b & 65535) + (a & 65535);
        return (b >> 16) + (a >> 16) + (c >> 16) << 16 | c & 65535
    };
    c.prototype.bit_rol = function(b, a) {
        return b << a | b >>> 32 - a
    };
    return c
} ();
md5.prototype.__class__ = "md5";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    BottomLayer = function(c) {
        function b() {
            c.call(this);
            this.init()
        }
        __extends(b, c);
        b.prototype.init = function() {
            this._bg = ResourceUtils.createBitmapByName("bottom_bg");
            this.addChild(this._bg);
            this._bg.y = 30;
            this._bg.anchorY = 1;
            this._layerContainer = new egret.DisplayObjectContainer;
            this.addChild(this._layerContainer);
            this._layerContainer.y = 10;
            this._touchArea = new egret.Sprite;
            this._touchArea.width = 80;
            this._touchArea.height = 80;
            this._touchArea.x = 555;
            this._touchArea.y = -400;
            this._touchArea.touchEnabled = !0;
            this.addChild(this._touchArea);
            var a = ResourceUtils.createBitmapFromSheet("bottom_btn");
            a.anchorX = a.anchorY = 0.5;
            a.x = 40;
            a.y = 40;
            this._touchArea.addChild(a);
            this._touchArea.addEventListener(egret.TouchEvent.TOUCH_TAP, this.showSkillLayer, this);
            this._skillContainer = new egret.DisplayObjectContainer;
            this.addChild(this._skillContainer);
            this._bottom = new BottomView;
            this._bottom.addOnClick(this.onTabSelect, this);
            this.addChild(this._bottom);
            this._bottom.selectTabWithType("main");
            addAction(Guide.HIDE_BOTTOM, this.hideBottom, this);
            addAction(Guide.SHOW_BOTTOM1, this.showBottom1, this);
            addAction(Guide.SHOW_BOTTOM2, this.showBottom2, this);
            addFilter("get_current_tag", this.getCurrentTag, this);
            addFilter(Guide.IS_SKILL_LAYER_EXSIT, this.isSkillExsit, this)
        };
        b.prototype.isSkillExsit = function() {
            return this._mainSkillLayer && this._mainSkillLayer.visible ? !0 : !1
        };
        b.prototype.getCurrentTag = function() {
            return this._currentTag
        };
        b.prototype.hideBottom = function() {
            this.showSkillLayer();
            this._bottom.visible = !1
        };
        b.prototype.showBottom1 = function() {
            this._bottom.visible = !0;
            this._bottom.hideTab()
        };
        b.prototype.showBottom2 = function() {
            this._bottom.showTab()
        };
        b.prototype.showSkillLayer = function() {
            this._layerContainer.visible = !1;
            null == this._mainSkillLayer && (this._mainSkillLayer = new SkillLayer, this._mainSkillLayer.y = -250);
            this._mainSkillLayer.visible = !0;
            this._bg.visible = !1;
            this._touchArea.visible = !1;
            this._bottom.unSelectAll();
            this._skillContainer.addChild(this._mainSkillLayer);
            DataCenter.getInstance().refreshCurrentDPS();
            doAction(Guide.CLICK_DOWN_JIANTOU)
        };
        b.prototype.hideSkillLayer = function() {
            this._mainSkillLayer && (this._mainSkillLayer.visible = !1);
            this._bg.visible = !0;
            this._touchArea.visible = !0;
            this._layerContainer.visible = !0
        };
        b.prototype.hideLayer = function(a) {
            a && (a.visible = !1)
        };
        b.prototype.showMallLayer = function() {
            null == this._paymentListLayer && (this._paymentListLayer = new PaymentListLayer, this._layerContainer.addChild(this._paymentListLayer), this._paymentListLayer.y = -250);
            this._paymentListLayer.visible = !0;
            this.hideLayer(this._heroLayer);
            this.hideLayer(this._skillListLayer);
            this.hideLayer(this._mainSkillLayer);
            this.hideLayer(this._rebornListLayer)
        };
        b.prototype.onTabSelect = function(a) {
            this._currentTag = a;
            this.hideSkillLayer();
            "main" == a ? (null == this._skillListLayer && (this._skillListLayer = new SkillListLayer, this._layerContainer.addChild(this._skillListLayer), this._skillListLayer.y = -250), this._skillListLayer.visible = !0, this.hideLayer(this._heroLayer), this.hideLayer(this._paymentListLayer), this.hideLayer(this._mainSkillLayer), this.hideLayer(this._rebornListLayer)) : "hero" == a ? (null == this._heroLayer && (this._heroLayer = new HeroListLayer, this._layerContainer.addChild(this._heroLayer), this._heroLayer.y = -250), this._heroLayer.visible = !0, this.hideLayer(this._skillListLayer), this.hideLayer(this._paymentListLayer), this.hideLayer(this._mainSkillLayer), this.hideLayer(this._rebornListLayer)) : "holy" == a ? (null == this._rebornListLayer && (this._rebornListLayer = new RebornListLayer, this._layerContainer.addChild(this._rebornListLayer), this._rebornListLayer.y = -250), this._rebornListLayer.visible = !0, this.hideLayer(this._heroLayer), this.hideLayer(this._paymentListLayer), this.hideLayer(this._skillListLayer), this.hideLayer(this._mainSkillLayer)) : ProxyUtils.platformInfo == platformType_wanba && -1 == ProxyUtils.jifen ? ProxyUtils.getTencentCoin(this, this.showMallLayer) : this.showMallLayer()
        };
        return b
    } (BaseContainer);
BottomLayer.prototype.__class__ = "BottomLayer";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    HeroDetailView = function(c) {
        function b() {
            c.call(this);
            this._skillItemList = [];
            this.initView()
        }
        __extends(b, c);
        b.prototype.initView = function() {
            var a = ResourceUtils.createBitmapByName("mask");
            a.x = -18;
            this.addChild(a);
            a.touchEnabled = !0;
            a.addEventListener(egret.TouchEvent.TOUCH_TAP, this.onCloseHandler, this);
            this._bg1 = ResourceUtils.createBitmapByName("main_skill_bg");
            this.addChild(this._bg1);
            this._bg1.touchEnabled = !0;
            this._bg1.x = 35;
            this._bg1.y = 0;
            var a = new SimpleButton,
                b = ResourceUtils.createBitmapFromSheet("close_btn", "commonRes");
            a.addChild(b);
            a.addOnClick(this.onCloseHandler, this);
            a.x = 520;
            a.y = 20;
            a.useZoomOut(!0);
            this.addChild(a);
            this._iconContainer = new egret.DisplayObjectContainer;
            this.addChild(this._iconContainer);
            this._iconContainer.x = 55;
            this._iconContainer.y = 63;
            this._staticContainer = new egret.DisplayObjectContainer;
            this.addChild(this._staticContainer);
            this._staticContainer.x = 19;
            this._staticContainer.y = 50;
            this._nameLabel = new egret.TextField;
            this._staticContainer.addChild(this._nameLabel);
            this._nameLabel.x = 126;
            this._nameLabel.y = 12;
            this._nameLabel.size = 32;
            this._lvLabel = new egret.TextField;
            this._staticContainer.addChild(this._lvLabel);
            this._lvLabel.text = "Lv.";
            this._lvLabel.x = 126;
            this._lvLabel.y = 50;
            this._lvLabel.size = 28;
            this._lvValLabel = new egret.TextField;
            this._staticContainer.addChild(this._lvValLabel);
            this._lvValLabel.x = 166;
            this._lvValLabel.y = 50;
            this._lvValLabel.size = 28;
            this._lvValLabel.textColor = 5292278;
            this._dpsLabel = new egret.TextField;
            this._staticContainer.addChild(this._dpsLabel);
            this._dpsLabel.text = "DPS:";
            this._dpsLabel.x = 256;
            this._dpsLabel.y = 50;
            this._dpsLabel.size = 28;
            this._dpsValLabel = new egret.TextField;
            this._staticContainer.addChild(this._dpsValLabel);
            this._dpsValLabel.x = 326;
            this._dpsValLabel.y = 50;
            this._dpsValLabel.size = 28;
            this._dpsValLabel.textColor = 14447689;
            this._desLabel = new egret.TextField;
            this._staticContainer.addChild(this._desLabel);
            this._desLabel.x = 41;
            this._desLabel.y = 130;
            this._desLabel.size = 20;
            this._desLabel.width = 480;
            this._desLabel.lineSpacing = 5;
            this._desLabel.text = "\u4f0a\u6cfd\u9677\u843d\u4e4b\u540e\uff0c\u6b64\u65f6\u4e00\u4f4d\u7ea2\u9f3b\u5b50\u7684\u82f1\u96c4\u51fa\u73b0\u5728\u4e86\u963f\u5c14\u74e6\u5927\u9646\uff0c\u4ed6\u51ed\u501f\u7740\u81ea\u8eab\u5f3a\u5927\u7684\u5b9e\u529b\u548c\u8d8a\u6765\u8d8a\u591a\u7684\u4f19\u4f34\uff0c\u4e3a\u62ef\u6551\u4f0a\u6cfd\u62ef\u6551\u963f\u5c14\u74e6\u5728\u52aa\u529b\u6218\u6597\u7740...";
            this._staticContainer.cacheAsBitmap = !0;
            this._skillContainer = new egret.DisplayObjectContainer;
            this.addChild(this._skillContainer);
            this._skillContainer.x = 70;
            this._skillContainer.y = 280;
            this._skillContainer.width = 515;
            for (a = 0; 7 > a; a++) b = new SkillDetailItemView,
                b.y = 90 * a,
                this._skillContainer.addChild(b),
                this._skillItemList.push(b);
            this._skillContainer.cacheAsBitmap = !0
        };
        b.prototype.refreshView = function(a) {
            this._model = a;
            this.updateHeroInfo();
            this.updateSkills();
            this._skillContainer.cacheAsBitmap = !0
        };
        b.prototype.updateHeroInfo = function() {
            this._nameLabel.text = this._model.config.name;
            this._desLabel.text = this._model.getDes();
            this._dpsValLabel.text = this._model.dps;
            this._lvValLabel.text = this._model.lv.toString();
            this._iconContainer.removeChildren();
            var a = ResourceUtils.createBitmapFromSheet(this._model.getRes(), "iconRes");
            this._iconContainer.addChild(a);
            a = ResourceUtils.createBitmapFromSheet("icon_decorate", "commonRes");
            a.x = -12;
            a.y = 10;
            this._iconContainer.addChild(a)
        };
        b.prototype.updateSkills = function() {
            var a = this._model.config.skill.split(","),
                b;
            for (b in a) {
                var c = parseInt(a[b]),
                    e = !1,
                    h;
                for (h in this._model.skillList) if (this._model.skillList[h].configId == c) {
                    e = !0;
                    break
                }
                var f = "icon_skill_" + RES.getRes("skillConfig")[c].iconID;
                this._skillItemList[b].refreshView(c, e, 0, !1, b, this._model, f)
            }
        };
        b.prototype.onMaskHandler = function() {};
        b.prototype.onCloseHandler = function() {
            GameMainLayer.instance.hideHeroDetailLayer()
        };
        b.prototype.dispose = function() {
            c.prototype.dispose.call(this)
        };
        return b
    } (BaseContainer);
HeroDetailView.prototype.__class__ = "HeroDetailView";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    MainHeroDetailView = function(c) {
        function b() {
            c.call(this);
            this._skillItemList = [];
            this.initView()
        }
        __extends(b, c);
        b.prototype.initView = function() {
            var a = ResourceUtils.createBitmapByName("mask");
            a.x = -18;
            this.addChild(a);
            a.touchEnabled = !0;
            a.addEventListener(egret.TouchEvent.TOUCH_TAP, this.onCloseHandler, this);
            this._bg1 = ResourceUtils.createBitmapByName("main_skill_bg");
            this.addChild(this._bg1);
            this._bg1.touchEnabled = !0;
            this._bg1.x = 35;
            this._bg1.y = 0;
            var a = new SimpleButton,
                b = ResourceUtils.createBitmapFromSheet("close_btn", "commonRes");
            a.addChild(b);
            a.addOnClick(this.onCloseHandler, this);
            a.x = 510;
            a.y = 20;
            a.useZoomOut(!0);
            this.addChild(a);
            a = ResourceUtils.createBitmapFromSheet("icon_hero_skill_1", "iconRes");
            b = ResourceUtils.createBitmapFromSheet("icon_decorate", "commonRes");
            b.x = -12;
            b.y = 10;
            this._iconContainer = new egret.DisplayObjectContainer;
            this.addChild(this._iconContainer);
            this._iconContainer.addChild(a);
            this._iconContainer.addChild(b);
            this._iconContainer.x = 55;
            this._iconContainer.y = 63;
            this._staticContainer = new egret.DisplayObjectContainer;
            this.addChild(this._staticContainer);
            this._staticContainer.x = 19;
            this._staticContainer.y = 50;
            this._nameLabel = new egret.TextField;
            this._staticContainer.addChild(this._nameLabel);
            this._nameLabel.x = 126;
            this._nameLabel.y = 12;
            this._nameLabel.size = 32;
            this._lvLabel = new egret.TextField;
            this._staticContainer.addChild(this._lvLabel);
            this._lvLabel.text = "Lv.";
            this._lvLabel.x = 126;
            this._lvLabel.y = 50;
            this._lvLabel.size = 28;
            this._lvValLabel = new egret.TextField;
            this._staticContainer.addChild(this._lvValLabel);
            this._lvValLabel.x = 166;
            this._lvValLabel.y = 50;
            this._lvValLabel.size = 28;
            this._lvValLabel.textColor = 5292278;
            this._dpsLabel = new egret.TextField;
            this._staticContainer.addChild(this._dpsLabel);
            this._dpsLabel.text = "DPS:";
            this._dpsLabel.x = 256;
            this._dpsLabel.y = 50;
            this._dpsLabel.size = 28;
            this._dpsValLabel = new egret.TextField;
            this._staticContainer.addChild(this._dpsValLabel);
            this._dpsValLabel.x = 326;
            this._dpsValLabel.y = 50;
            this._dpsValLabel.size = 28;
            this._dpsValLabel.textColor = 14447689;
            this._desLabel = new egret.TextField;
            this._staticContainer.addChild(this._desLabel);
            this._desLabel.x = 41;
            this._desLabel.y = 130;
            this._desLabel.size = 20;
            this._desLabel.width = 480;
            this._desLabel.lineSpacing = 5;
            this._desLabel.text = "\u4f0a\u6cfd\u9677\u843d\u4e4b\u540e\uff0c\u6b64\u65f6\u4e00\u4f4d\u7ea2\u9f3b\u5b50\u7684\u82f1\u96c4\u51fa\u73b0\u5728\u4e86\u963f\u5c14\u74e6\u5927\u9646\uff0c\u4ed6\u51ed\u501f\u7740\u81ea\u8eab\u5f3a\u5927\u7684\u5b9e\u529b\u548c\u8d8a\u6765\u8d8a\u591a\u7684\u4f19\u4f34\uff0c\u4e3a\u62ef\u6551\u4f0a\u6cfd\u62ef\u6551\u963f\u5c14\u74e6\u5728\u52aa\u529b\u6218\u6597\u7740...";
            this._staticContainer.cacheAsBitmap = !0;
            this._skillContainer = new egret.DisplayObjectContainer;
            this.addChild(this._skillContainer);
            this._skillContainer.x = 65;
            this._skillContainer.y = 285;
            this._skillContainer.width = 515;
            for (a = 0; 6 > a; a++) b = new SkillDetailItemView,
                this._skillContainer.addChild(b),
                b.y = 105 * a,
                this._skillContainer.addChild(b),
                this._skillItemList.push(b);
            this._skillContainer.cacheAsBitmap = !0
        };
        b.prototype.refreshView = function(a) {
            this._model = a;
            this.updateHeroInfo();
            this.updateSkills();
            this._skillContainer.cacheAsBitmap = !0
        };
        b.prototype.updateHeroInfo = function() {
            this._nameLabel.text = this._model.config.name;
            this._dpsValLabel.text = this._model.dps;
            this._lvValLabel.text = this._model.lv.toString()
        };
        b.prototype.updateSkills = function() {
            for (var a in this._model.skillList) this._skillItemList[a].refreshView(this._model.skillList[a].configId, 0 < this._model.skillList[a].lv ? !0 : !1, this._model.skillList[a].lv, !0, a, this._model, "icon_hero_skill_" + 2 * (parseInt(a) + 1))
        };
        b.prototype.onMaskHandler = function() {};
        b.prototype.onCloseHandler = function() {
            GameMainLayer.instance.hideMainHeroDetailLayer()
        };
        b.prototype.dispose = function() {
            c.prototype.dispose.call(this)
        };
        return b
    } (BaseContainer);
MainHeroDetailView.prototype.__class__ = "MainHeroDetailView";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    RebornSkillDetailLayer = function(c) {
        function b() {
            c.call(this);
            this.initView()
        }
        __extends(b, c);
        b.prototype.initView = function() {
            var a = ResourceUtils.createBitmapByName("mask");
            this.addChild(a);
            a.touchEnabled = !0;
            a.addEventListener(egret.TouchEvent.TOUCH_TAP, this.onMaskHandler, this);
            a = ResourceUtils.createBitmapByName("reborn_skill");
            this.addChild(a);
            a.x = 23;
            a.y = 0;
            var b = new SimpleButton,
                c = ResourceUtils.createBitmapFromSheet("close_btn", "commonRes");
            b.addChild(c);
            b.addOnClick(this.onClickHandler, this);
            b.x = 543;
            b.y = 70;
            b.useZoomOut(!0);
            this.addChild(b);
            this._mainContainer = new egret.DisplayObjectContainer;
            this._mainContainer.x = 0;
            this._mainContainer.y = 110;
            this._mainContainer.width = a.width;
            this.addChild(this._mainContainer);
            this._skillCoin = new egret.DisplayObjectContainer;
            this._skillCoin.x = 62;
            this._skillCoin.y = 30;
            this._skillCoin.scaleX = this._skillCoin.scaleY = 1.8;
            this._mainContainer.addChild(this._skillCoin);
            this._descLabel = new egret.TextField;
            this._descLabel.text = "\u91cd\u751f\u4ee5\u540e\u5723\u7269\u4f9d\u7136\u4fdd\u7559,\u5347\u7ea7\u5723\u7269\u53ef\u4ee5\u83b7\u5f97\u66f4\u5f3a\u80fd\u529b";
            this._descLabel.size = 20;
            this._mainContainer.addChild(this._descLabel);
            this._descLabel.x = 70;
            this._descLabel.y = 275;
            this._nameLabel = new egret.TextField;
            this._nameLabel.text = "\u6211\u65f6\u6280\u80fd\u540d\u5b57";
            this._nameLabel.size = 24;
            this._mainContainer.addChild(this._nameLabel);
            this._nameLabel.x = 210;
            this._nameLabel.y = 30;
            this._lvTitle = new egret.TextField;
            this._mainContainer.addChild(this._lvTitle);
            this._lvTitle.text = "Lv.";
            this._lvTitle.size = 20;
            this._lvTitle.x = 210;
            this._lvTitle.y = 65;
            this._lvLabel = new egret.TextField;
            this._mainContainer.addChild(this._lvLabel);
            this._lvLabel.size = 20;
            this._lvLabel.textColor = 5292278;
            this._lvLabel.x = 240;
            this._lvLabel.y = 65;
            this._maxlvLabel = new egret.TextField;
            this._mainContainer.addChild(this._maxlvLabel);
            this._maxlvLabel.size = 20;
            this._maxlvLabel.x = 280;
            this._maxlvLabel.y = 65;
            this._effectLabel1 = new egret.TextField;
            this._effectLabel1.size = 20;
            this._effectLabel1.x = 210;
            this._effectLabel1.y = 90;
            this._mainContainer.addChild(this._effectLabel1);
            this._effectLabel2 = new egret.TextField;
            this._effectLabel2.size = 20;
            this._effectLabel2.x = 210;
            this._effectLabel2.y = 115;
            this._mainContainer.addChild(this._effectLabel2);
            this._nextlvLabel = new egret.TextField;
            this._mainContainer.addChild(this._nextlvLabel);
            this._nextlvLabel.size = 20;
            this._nextlvLabel.x = 210;
            this._nextlvLabel.y = 150;
            this._nextlvLabel.textColor = 5292278;
            this._nextEffectLabel1 = new egret.TextField;
            this._nextEffectLabel1.size = 20;
            this._nextEffectLabel1.x = 210;
            this._nextEffectLabel1.y = 175;
            this._mainContainer.addChild(this._nextEffectLabel1);
            this._nextEffectLabel2 = new egret.TextField;
            this._nextEffectLabel2.size = 20;
            this._nextEffectLabel2.x = 210;
            this._nextEffectLabel2.y = 197;
            this._mainContainer.addChild(this._nextEffectLabel2);
            this._btnContainer = new SimpleButton;
            this._btnContainer.anchorX = this._btnContainer.anchorY = 0.5;
            a = ResourceUtils.createBitmapFromSheet("buy_btn", "commonRes");
            this._btnContainer.addChild(a);
            this._btnContainer.x = 310;
            this._btnContainer.y = 440;
            this._btnTitle1 = new egret.TextField;
            this._btnTitle1.text = "\u62c6  \u89e3";
            this._btnTitle1.size = 24;
            this._btnTitle1.x = 60;
            this._btnTitle1.y = 18;
            this._btnContainer.addChild(this._btnTitle1);
            this._btnContainer.useZoomOut(!0);
            this._btnContainer.addOnClick(this.btnClick, this);
            this._mainContainer.addChild(this._btnContainer);
            this._moneyContainer = new egret.DisplayObjectContainer;
            this._moneyContainer.x = 240;
            this._moneyContainer.y = 385;
            this._moneyLabel = new egret.TextField;
            this._moneyLabel.text = "\u6d88\u801799";
            this._moneyLabel.x = 0;
            this._moneyLabel.y = 0;
            this._moneyLabel.size = 20;
            this._moneyContainer.addChild(this._moneyLabel);
            this._moneyCoin = ResourceUtils.createBitmapFromSheet("gold_icon", "commonRes");
            this._moneyCoin.y = 0;
            this._moneyCoin.scaleX = this._moneyCoin.scaleY = 0.5;
            this._moneyCoin.x = this._moneyLabel.width + 2;
            this._moneyContainer.addChild(this._moneyCoin);
            this._btnTitle2 = new egret.TextField;
            this._btnTitle2.text = "\u8fd4\u8fd840";
            this._btnTitle2.size = 20;
            this._btnTitle2.x = this._moneyLabel.width + 0.5 * this._moneyCoin.width + 4;
            this._btnTitle2.y = 0;
            this._moneyContainer.addChild(this._btnTitle2);
            this._holyCoin = ResourceUtils.createBitmapFromSheet("holy_icon", "commonRes");
            this._holyCoin.y = -4;
            this._holyCoin.scaleX = this._holyCoin.scaleY = 0.7;
            this._holyCoin.x = this._moneyLabel.width + 0.5 * this._moneyCoin.width + this._btnTitle2.width + 6;
            this._moneyContainer.addChild(this._holyCoin);
            this._mainContainer.addChild(this._moneyContainer)
        };
        b.prototype.refreshView = function(a) {
            this._nameLabel.text = a.config.name;
            this._lvLabel.text = a.lv.toString();
            this._effectLabel1.text = a.getDes1();
            this._effectLabel2.text = a.getDes2();
            this._nextEffectLabel1.visible = !1;
            this._nextEffectLabel2.visible = !1;
            var b = a.lvLimit();
            0 == b ? (this._maxlvLabel.visible = !1, this._nextEffectLabel1.visible = !0, this._nextEffectLabel2.visible = !0, this._nextEffectLabel1.text = a.getDes1(a.lv + 1), this._nextEffectLabel2.text = a.getDes2(a.lv + 1), this._nextlvLabel.text = "\u4e0b\u4e00\u7b49\u7ea7:") : (this._maxlvLabel.visible = !0, this._maxlvLabel.text = "(\u6700\u5927\u7b49\u7ea7:" + b + ")", b > a.lv ? (this._nextEffectLabel1.visible = !0, this._nextEffectLabel2.visible = !0, this._nextEffectLabel1.text = a.getDes1(a.lv + 1), this._nextEffectLabel2.text = a.getDes2(a.lv + 1), this._nextlvLabel.text = "\u4e0b\u4e00\u7b49\u7ea7:") : this._nextlvLabel.text = "\u7b49\u7ea7\u8fbe\u5230\u4e0a\u9650");
            this._skillModel = a;
            this._moneyLabel.text = "\u6d88\u8017" + a.getNeedGc();
            this._btnTitle2.text = "\u8fd4\u8fd8" + a.getTotalCost();
            this._skillCoin.removeChildren();
            a = ResourceUtils.createBitmapFromSheet(this._skillModel.getRes(), "iconRes");
            this._skillCoin.addChild(a);
            this._moneyCoin.x = this._moneyLabel.width + 2;
            this._btnTitle2.x = this._moneyLabel.width + 0.5 * this._moneyCoin.width + 4;
            this._holyCoin.x = this._moneyLabel.width + 0.5 * this._moneyCoin.width + this._btnTitle2.width + 6
        };
        b.prototype.onMaskHandler = function() {};
        b.prototype.btnClick = function() {
            DataCenter.getInstance().hasEnoughGc(this._skillModel.getNeedGc()) ? (DataCenter.getInstance().destroyRebornSkill(this._skillModel), this.onClickHandler()) : GameMainLayer.instance.showPaymentRecommendLayer()
        };
        b.prototype.onClickHandler = function() {
            GameMainLayer.instance.hideRebornSkillDetailLayer()
        };
        return b
    } (BaseContainer);
RebornSkillDetailLayer.prototype.__class__ = "RebornSkillDetailLayer";
var AchieveData = function() {
    return function() {}
} ();
AchieveData.prototype.__class__ = "AchieveData";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    AchievementLayer = function(c) {
        function b() {
            c.call(this);
            this.init();
            addAction(Const.ADD_DIAMOND, this.addDiamond, this);
            addAction(Const.CHANGE_DIAMOND, this.changeDiamond, this)
        }
        __extends(b, c);
        b.prototype.addDiamond = function(a) {
            var b = a.parent.localToGlobal(a.x, a.y),
                b = this.globalToLocal(b.x, b.y, b);
            this.addChild(a);
            a.x = b.x;
            a.y = b.y;
            var c = GameUtils.random(300, 360),
                e = b.x + 200 * Math.cos(c),
                b = b.y + 200 * Math.sin(c);
            TweenLite.to(a, 1.5, {
                bezier: {
                    values: [{
                        x: e,
                        y: b
                    },
                        {
                            x: this._gold.x,
                            y: this._gold.y
                        }],
                    autoRotation: !0
                },
                onComplete: function() {
                    GameUtils.removeFromParent(a);
                    DataCenter.getInstance().changeGc(a.num)
                }
            })
        };
        b.prototype.changeDiamond = function() {
            this._goldLabel.text = DataCenter.getInstance().getGcNum()
        };
        b.prototype.close = function() {
            GameMainLayer.instance.setAchieveLayerVisible(!1)
        };
        b.prototype.init = function() {
            var a = ResourceUtils.createBitmapByName("achieve_bg");
            a.touchEnabled = !0;
            a.y = 0;
            this.addChild(a);
            a = new SimpleButton;
            a.addOnClick(this.close, this);
            a.x = 598;
            a.y = 55;
            a.anchorX = a.anchorY = 0.5;
            a.useZoomOut(!0);
            a.addChild(ResourceUtils.createBitmapFromSheet("close_btn"));
            this.addChild(a);
            this._infoContainer = new egret.DisplayObjectContainer;
            this.addChild(this._infoContainer);
            a = new egret.TextField;
            a.text = "\u6210 \u5c31";
            a.size = 35;
            a.x = 325;
            a.y = 65;
            a.textColor = 0;
            a.anchorX = a.anchorY = 0.5;
            this._infoContainer.addChild(a);
            a = new egret.TextField;
            a.text = "\u8fbe\u6210\u76ee\u6807\u83b7\u53d6\u5956\u52b1\uff01";
            a.textColor = 16777215;
            a.size = 22;
            a.x = 80;
            a.y = 130;
            this._infoContainer.addChild(a);
            this._gold = ResourceUtils.createBitmapFromSheet("gold_icon");
            this._gold.anchorX = this._gold.anchorY = 0.5;
            this._gold.x = 535;
            this._gold.y = 148;
            this._infoContainer.addChild(this._gold);
            this._goldLabel = new egret.TextField;
            this._goldLabel.text = DataCenter.getInstance().getGcNum();
            this._goldLabel.anchorX = 1;
            this._goldLabel.x = 505;
            this._goldLabel.y = 130;
            this._goldLabel.size = 24;
            this._infoContainer.addChild(this._goldLabel);
            this._achieveTable = new TableView;
            this._achieveTable.y = 205;
            this._achieveTable.x = 13;
            this.addChild(this._achieveTable);
            this._achieveTable.width = GameUtils.getWinWidth();
            this._achieveTable.height = 750;
            this._achieveTable.setList(DataCenter.getInstance().achieveList, Direction.VERTICAL, this, GameUtils.getWinWidth(), 110)
        };
        b.prototype.reload = function() {
            this._achieveTable.reloadData(DataCenter.getInstance().achieveList);
            this._goldLabel.text = DataCenter.getInstance().getGcNum()
        };
        b.prototype.createItemRenderer = function() {
            return new AchieveItemLayer
        };
        b.prototype.updateItemRenderer = function(a, b, c) {
            a.refreshView(b, c)
        };
        b.prototype.dispose = function() {
            c.prototype.dispose.call(this);
            removeEventsByTarget(this)
        };
        return b
    } (BaseContainer);
AchievementLayer.prototype.__class__ = "AchievementLayer";
var AchievementButton = function(c) {
    function b() {
        c.call(this);
        this.anchorY = this.anchorX = 0.5;
        this.init()
    }
    __extends(b, c);
    b.prototype.init = function() {
        var a = ResourceUtils.createBitmapFromSheet("achieve_btn");
        this.addChild(a);
        a = ResourceUtils.createBitmapFromSheet("cup");
        a.scaleY = a.scaleX = 0.75;
        a.x = 10;
        a.y = 8;
        this.addChild(a);
        this._label = new egret.TextField;
        this.addChild(this._label);
        this._label.text = "\u6210 \u5c31";
        this._label.size = 35;
        this._label.x = 180;
        this._label.y = 6;
        this._label.textAlign = egret.HorizontalAlign.CENTER;
        this._label.anchorX = 0.5
    };
    return b
} (SimpleButton);
AchievementButton.prototype.__class__ = "AchievementButton";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    CodeAwardLayer = function(c) {
        function b() {
            c.call(this);
            this.initView()
        }
        __extends(b, c);
        b.prototype.initView = function() {
            this._container = new egret.DisplayObjectContainer;
            this.addChild(this._container);
            var a = ResourceUtils.createBitmapByName("mask");
            this._container.addChild(a);
            a.touchEnabled = !0;
            a = ResourceUtils.createBitmapByName("share_bg");
            this._container.addChild(a);
            a.x = 25;
            a = new SimpleButton;
            a.anchorX = a.anchorY = 0.5;
            var b = ResourceUtils.createBitmapFromSheet("close_btn");
            a.addChild(b);
            a.addOnClick(this.close, this);
            a.useZoomOut(!0);
            a.x = 590;
            a.y = 160;
            this.addChild(a);
            a = ResourceUtils.createBitmapFromSheet("share_info_bg");
            a.x = 65;
            a.y = 220;
            this._container.addChild(a);
            a = ResourceUtils.createBitmapFromSheet("share_tip");
            a.x = 55;
            a.y = 228;
            this._container.addChild(a);
            a = new egret.TextField;
            a.size = 26;
            this._container.addChild(a);
            a.textFlow = (new egret.HtmlTextParser).parser('\u641c\u7d22\u5e76\u5173\u6ce8\u5fae\u4fe1\u516c\u4f17\u53f7:\n<font color="0xff0000">changmengyouxi</font>\uff0c\u53d1\u9001"<font color="0xff0000">\u6469\u5c14</font>",\n\u83b7\u53d6\u6fc0\u6d3b\u7801,\u5373\u53ef\u83b7\u5f97\u94bb\u77f3\u5956\u52b1!');
            a.lineSpacing = 10;
            a.anchorX = 0;
            a.anchorY = 1;
            a.textColor = 16777215;
            a.x = 200;
            a.y = 335;
            this._offsetContainer = new egret.DisplayObjectContainer;
            this._container.addChild(this._offsetContainer);
            a = ResourceUtils.createBitmapFromSheet("share_award_bg");
            a.x = 125;
            a.y = 400;
            a.scaleX = 1.1;
            this._offsetContainer.addChild(a);
            a = ResourceUtils.createBitmapByName("gold_border");
            a.x = 140;
            a.y = 415;
            this._offsetContainer.addChild(a);
            a = ResourceUtils.createBitmapFromSheet("icon_payment_6", "iconRes");
            a.x = 143;
            a.y = 420;
            this._offsetContainer.addChild(a);
            this._awardLabel = new egret.TextField;
            this._offsetContainer.addChild(this._awardLabel);
            this._awardLabel.x = 260;
            this._awardLabel.y = 440;
            this._awardLabel.size = 26;
            this._awardLabel.textFlow = (new egret.HtmlTextParser).parser('<font color="0xffffff">\u5956\u52b1\uff1a</font><font color="0xEBA111">100\u94bb\u77f3</font>');
            a = ResourceUtils.createBitmapByName("share_input_bg");
            a.x = 90;
            a.y = 550;
            this._offsetContainer.addChild(a);
            this._container.cacheAsBitmap = !0;
            this._inputLabel = new egret.TextField;
            this._inputLabel.type = egret.TextFieldType.INPUT;
            this._inputLabel.width = 400;
            this._inputLabel.height = 100;
            this._inputLabel.text = "\u8f93\u5165\u6fc0\u6d3b\u7801...";
            this._inputLabel.x = 100;
            this._inputLabel.y = 565;
            this._inputLabel.size = 26;
            this._offsetContainer.addChild(this._inputLabel);
            this._offsetContainer.y = -25;
            this.addChild(GameUtils.createButton("\u9886\u53d6\u5956\u52b1", "unlock_skill_btn", this.awardHandler.bind(this), 170, 670, 170, 69, -5, -5));
            this.addChild(GameUtils.createButton("\u5173\u6ce8\u516c\u4f17\u53f7", "long_btn", this.weixi.bind(this), 470, 670, 170, 69, 0, -5))
        };
        b.prototype.weixi = function() {
            PlatformFactory.getPlatform().name == PlatformTypes.WEIXIN && GameUtils.notEmpty(ProxyUtils.winxinUrl) && (location.href = ProxyUtils.winxinUrl)
        };
        b.prototype.awardHandler = function() {
            "" != this._inputLabel.text && PlatformFactory.getPlatform().codeAward(this._inputLabel.text)
        };
        b.prototype.close = function() {
            GameMainLayer.instance.hideCodeAwardLayer()
        };
        return b
    } (BaseContainer);
CodeAwardLayer.prototype.__class__ = "CodeAwardLayer";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    HeroDieLayer = function(c) {
        function b() {
            c.call(this);
            this.init()
        }
        __extends(b, c);
        b.prototype.init = function() {
            var a = ResourceUtils.createBitmapByName("mask");
            this.addChild(a);
            a.touchEnabled = !0;
            a.addEventListener(egret.TouchEvent.TOUCH_TAP, this.onMaskHandler, this);
            a = ResourceUtils.createBitmapByName("die_bg");
            this.addChild(a);
            a.x = 24;
            a.y = 0;
            this._mainContainer = new egret.DisplayObjectContainer;
            this.addChild(this._mainContainer);
            this._mainContainer.x = 24;
            this._mainContainer.y = 153;
            this._mainContainer.width = a.width;
            var a = new SimpleButton,
                b = ResourceUtils.createBitmapFromSheet("close_btn", "commonRes");
            a.addChild(b);
            a.addOnClick(this.onClickHandler, this);
            a.x = 525;
            a.y = -30;
            a.useZoomOut(!0);
            this._mainContainer.addChild(a);
            this._heroIconContainer = new egret.DisplayObjectContainer;
            this._mainContainer.addChild(this._heroIconContainer);
            this._heroNameLabel = new egret.TextField;
            this._heroNameLabel.textColor = 5292278;
            this._heroNameLabel.size = 28;
            this._heroNameLabel.y = 140;
            this._mainContainer.addChild(this._heroNameLabel);
            this._bossNameLabel = new egret.TextField;
            this._bossNameLabel.textColor = 14447689;
            this._bossNameLabel.size = 28;
            this._bossNameLabel.y = 140;
            this._mainContainer.addChild(this._bossNameLabel);
            this._kill1Label = new egret.TextField;
            this._kill1Label.text = "\u88ab";
            this._kill1Label.y = 140;
            this._kill1Label.size = 28;
            this._mainContainer.addChild(this._kill1Label);
            this._kill2Label = new egret.TextField;
            this._kill2Label.text = "\u6740\u6389\u4e86 !";
            this._kill2Label.y = 140;
            this._kill2Label.size = 28;
            this._mainContainer.addChild(this._kill2Label);
            this._descLabel1 = new egret.TextField;
            this._descLabel1.text = "\u6ca1\u6709\u82f1\u96c4\u7684\u8bdd,";
            this._descLabel1.size = 21;
            this._descLabel1.x = Math.floor((this._mainContainer.width - this._descLabel1.width) / 2);
            this._descLabel1.y = 250;
            this._mainContainer.addChild(this._descLabel1);
            this._descLabel2 = new egret.TextField;
            this._descLabel2.text = "\u83b7\u5f97\u7684\u91d1\u5e01\u6570\u91cf\u548c\u9020\u6210\u7684\u4f24\u5bb3\u90fd\u4f1a\u5927\u5e45\u51cf\u5c11";
            this._descLabel2.size = 21;
            this._descLabel2.x = Math.floor((this._mainContainer.width - this._descLabel2.width) / 2);
            this._descLabel2.y = 275;
            this._mainContainer.addChild(this._descLabel2);
            this._descLabel3 = new egret.TextField;
            this._descLabel3.text = "!";
            this._descLabel3.size = 21;
            this._descLabel3.x = Math.floor((this._mainContainer.width - this._descLabel3.width) / 2);
            this._descLabel3.y = 295;
            this._mainContainer.addChild(this._descLabel3);
            this._dpsLabel = new egret.TextField;
            this._dpsLabel.textColor = 5292278;
            this._dpsLabel.text = "DPS: ";
            this._dpsLabel.size = 18;
            this._dpsLabel.y = 350;
            this._mainContainer.addChild(this._dpsLabel);
            this._dpsValLabel = new egret.TextField;
            this._dpsValLabel.textColor = 14447689;
            this._dpsValLabel.size = 18;
            this._dpsValLabel.text = "-10.96B/\u79d2";
            this._dpsValLabel.y = 350;
            this._mainContainer.addChild(this._dpsValLabel);
            this._dpsLabel.x = Math.floor((this._mainContainer.width - this._dpsLabel.width - this._dpsValLabel.width) / 2);
            this._dpsValLabel.x = this._dpsLabel.x + this._dpsLabel.width;
            this._btnContainer = new egret.DisplayObjectContainer;
            a = ResourceUtils.createBitmapFromSheet("big_long_btn", "commonRes");
            a.x = 0;
            a.y = 0;
            this._btnContainer.x = Math.floor((this._mainContainer.width - a.width) / 2);
            this._btnContainer.y = 560;
            this._btnContainer.addChild(a);
            this._btnTitle1 = new egret.TextField;
            this._btnTitle1.text = "\u73b0\u5728\u590d\u6d3b \uff01";
            this._btnTitle1.size = 28;
            this._btnTitle1.x = 30;
            this._btnTitle1.y = 20;
            this._btnTitle1.textColor = 0;
            this._btnContainer.addChild(this._btnTitle1);
            this._moneyCoin = ResourceUtils.createBitmapFromSheet("gold_icon", "commonRes");
            this._moneyCoin.x = 175;
            this._moneyCoin.y = 32;
            this._moneyCoin.scaleX = this._moneyCoin.scaleY = 0.5;
            this._btnContainer.addChild(this._moneyCoin);
            this._moneyLabel = new egret.TextField;
            this._moneyLabel.text = "50";
            this._moneyLabel.x = 177;
            this._moneyLabel.y = 10;
            this._moneyLabel.size = 18;
            this._btnContainer.addChild(this._moneyLabel);
            this._touchArea = new egret.Sprite;
            this._touchArea.width = this._btnContainer.width;
            this._touchArea.height = this._btnContainer.height;
            this._touchArea.touchEnabled = !0;
            this._btnContainer.addChild(this._touchArea);
            this._touchArea.addEventListener(egret.TouchEvent.TOUCH_TAP, this.btnClick, this);
            this._mainContainer.addChild(this._btnContainer);
            this._timeTitleLabel = new egret.TextField;
            this._timeTitleLabel.text = "\u8ddd\u79bb\u4f60\u5f97\u82f1\u96c4\u590d\u6d3b\u8fd8\u6709\uff1a";
            this._timeTitleLabel.x = 130;
            this._timeTitleLabel.y = 515;
            this._timeTitleLabel.size = 21;
            this._mainContainer.addChild(this._timeTitleLabel);
            this._timeLabel = new egret.TextField;
            this._timeLabel.x = this._timeTitleLabel.x + this._timeTitleLabel.width;
            this._timeLabel.y = 515;
            this._timeLabel.size = 21;
            this._timeLabel.textColor = 14447689;
            this._mainContainer.addChild(this._timeLabel)
        };
        b.prototype.refreshView = function(a) {
            this._hero = a;
            this._dpsValLabel.text = "-" + a.dps + "/\u79d2";
            this.tickTime();
            this._timeID = Time.setInterval(this.tickTime, this, 1E3);
            this.setHeroInfo()
        };
        b.prototype.setHeroInfo = function() {
            var a = ResourceUtils.createBitmapFromSheet(this._hero.getRes(), "iconRes");
            this._heroIconContainer.removeChildren();
            this._heroIconContainer.addChild(a);
            this._heroIconContainer.x = Math.floor((this._mainContainer.width - this._heroIconContainer.width) / 2);
            this._heroIconContainer.y = 30;
            this._heroNameLabel.text = this._hero.config.name;
            this._bossNameLabel.text = "\u9570\u5200\u624b";
            this._heroNameLabel.x = Math.floor((this._mainContainer.width - (this._heroNameLabel.width + this._kill1Label.width + this._bossNameLabel.width + this._kill2Label.width)) / 2);
            this._kill1Label.x = this._heroNameLabel.x + this._heroNameLabel.width + 3;
            this._bossNameLabel.x = this._kill1Label.x + this._kill1Label.width + 3;
            this._kill2Label.x = this._bossNameLabel.x + this._bossNameLabel.width + 3
        };
        b.prototype.checkMoney = function(a) {
            this._needMoney = a = Math.ceil(a / 600);
            this._moneyLabel.text = a.toString()
        };
        b.prototype.tickTime = function() {
            var a = this._hero.getRebornTime();
            if (0 >= a) this.onClickHandler();
            else this.checkMoney(a),
                this._timeLabel.text = GameUtils.formatTime1(a)
        };
        b.prototype.onMaskHandler = function() {};
        b.prototype.btnClick = function() {
            DataCenter.getInstance().hasEnoughGc(this._needMoney) ? (DataCenter.getInstance().reduceGc(this._needMoney), this._hero.reborn(), this.onClickHandler()) : GameMainLayer.instance.showPaymentRecommendLayer()
        };
        b.prototype.onClickHandler = function() {
            GameMainLayer.instance.hideDieLayer();
            this._timeID && Time.clearInterval(this._timeID)
        };
        return b
    } (BaseContainer);
HeroDieLayer.prototype.__class__ = "HeroDieLayer";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    HolyAnimationLayer = function(c) {
        function b() {
            c.call(this);
            this._curIdx = 1;
            this.initView()
        }
        __extends(b, c);
        b.prototype.initView = function() {
            var a = new egret.Bitmap;
            a.width = GameUtils.getWinWidth();
            a.height = GameUtils.getWinHeight();
            this.addChild(a);
            a.touchEnabled = !0;
            a.addEventListener(egret.TouchEvent.TOUCH_TAP, this.onClickHandler, this);
            this._container = new egret.DisplayObjectContainer;
            this.addChild(this._container)
        };
        b.prototype.refreshAnimation = function(a) {
            this._model = a;
            this._container.removeChildren();
            this._curIdx = 1;
            GameUtils.preloadAnimation("buy_reborn_json", this.addAnimation, this)
        };
        b.prototype.addAnimation = function() {
            this._action = new Movie("buy_reborn_json", "buy_reborn");
            this._action.x = GameUtils.getWinWidth() / 2;
            this._action.y = GameUtils.getWinHeight() / 2 - 250;
            this._container.addChild(this._action);
            this._action.autoRemove = !1;
            this._action.setCallback(this.onMoveCallback, this);
            this._action.play("1");
            this.addBones()
        };
        b.prototype.addBones = function() {
            var a = new egret.TextField;
            a.text = this._model.config.name;
            a.anchorX = a.anchorY = 0.5;
            a.textColor = 2040361;
            a.size = 35;
            this._action.addBoneDisplay("NameContainer", a);
            a = ResourceUtils.createBitmapFromSheet(this._model.getRes(), "iconRes");
            a.x = -35;
            a.y = -35;
            this._action.addBoneDisplay("IconContainer", a)
        };
        b.prototype.onMoveCallback = function(a, b) {
            if (a == Movie.PLAY_COMPLETE && (1 == this._curIdx && (this._action.play("2"), this.addBones(), this._curIdx += 1), 3 == this._curIdx)) this.onCloseHandler()
        };
        b.prototype.onCloseHandler = function() {
            GameMainLayer.instance.hideHolyAnimation()
        };
        b.prototype.onClickHandler = function() {
            2 == this._curIdx && (this._action.play("3"), this._curIdx = 3)
        };
        return b
    } (BaseContainer);
HolyAnimationLayer.prototype.__class__ = "HolyAnimationLayer";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    MainSkillCDLayer = function(c) {
        function b() {
            c.call(this);
            this.initView()
        }
        __extends(b, c);
        b.prototype.initView = function() {
            var a = ResourceUtils.createBitmapByName("mask");
            this.addChild(a);
            a.touchEnabled = !0;
            a.addEventListener(egret.TouchEvent.TOUCH_TAP, this.onMaskHandler, this);
            a = ResourceUtils.createBitmapByName("paymen_recommend");
            a.x = 20;
            this.addChild(a);
            a = new SimpleButton;
            a.anchorX = a.anchorY = 0.5;
            var b = ResourceUtils.createBitmapFromSheet("leave_battle");
            a.addChild(b);
            b = new egret.TextField;
            b.text = "\u5426";
            b.size = 22;
            b.x = 70;
            b.y = 20;
            a.addChild(b);
            a.useZoomOut(!0);
            a.addOnClick(this.onCloseHandler, this);
            a.x = 180;
            a.y = 500;
            this.addChild(a);
            a = new SimpleButton;
            a.anchorX = a.anchorY = 0.5;
            b = ResourceUtils.createBitmapFromSheet("reborn_lvup_btn");
            a.addChild(b);
            b = new egret.TextField;
            b.text = "\u662f";
            b.size = 22;
            b.x = 70;
            b.y = 20;
            a.addChild(b);
            a.useZoomOut(!0);
            a.addOnClick(this.onOKHandler, this);
            a.x = 450;
            a.y = 500;
            this.addChild(a);
            b = new egret.TextField;
            this.addChild(b);
            b.size = 24;
            b.x = 80;
            b.y = 280;
            b.text = "\u4f60\u60f3\u8981\u82b1\u8d39";
            a = new egret.TextField;
            this.addChild(a);
            a.size = 24;
            a.x = 230;
            a.y = 280;
            a.text = "\u94bb\u77f3\u91cd\u7f6e\u6b64\u6280\u80fd\u51b7\u5374\u5417?";
            this._costLabel = new egret.TextField;
            this.addChild(this._costLabel);
            this._costLabel.textColor = 16711680;
            this._costLabel.x = 200;
            this._costLabel.size = 24;
            this._costLabel.y = 280
        };
        b.prototype.refreshView = function(a) {
            var b = SkillModel.createByConfigId(a).getCDTime(),
                c = DataCenter.getInstance().totalData.mainSkillCD[a],
                e = (new Date).getTime(),
                b = 2 * Math.ceil((b - Math.floor((e - c) / 1E3)) / 60);
            this._costLabel.text = b.toString();
            this._configId = a;
            this._costNum = b
        };
        b.prototype.onMaskHandler = function() {};
        b.prototype.onCloseHandler = function() {
            GameMainLayer.instance.hideSkillCDLayer()
        };
        b.prototype.onOKHandler = function() {
            DataCenter.getInstance().reduceGc(this._costNum);
            doAction(Const.CLEAN_SKILL_CD, this._configId);
            this.onCloseHandler()
        };
        return b
    } (BaseContainer);
MainSkillCDLayer.prototype.__class__ = "MainSkillCDLayer";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    MaskLayer = function(c) {
        function b() {
            c.call(this);
            this.initView()
        }
        __extends(b, c);
        b.prototype.initView = function() {
            var a = ResourceUtils.createBitmapByName("mask");
            this.addChild(a);
            a.touchEnabled = !0;
            a = new egret.TextField;
            a.x = 320;
            a.y = 480;
            a.anchorX = 0.5;
            this.addChild(a);
            a.text = "\u8bf7\u5728\u826f\u597d\u7684\u7f51\u7edc\u73af\u5883\u4e0b\u6e38\u620f"
        };
        return b
    } (BaseContainer);
MaskLayer.prototype.__class__ = "MaskLayer";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    ResetGameConfirmLayer = function(c) {
        function b() {
            c.call(this);
            this._awardNum = 0;
            this.initView()
        }
        __extends(b, c);
        b.prototype.initView = function() {
            var a = ResourceUtils.createBitmapByName("mask");
            this.addChild(a);
            a.touchEnabled = !0;
            a.addEventListener(egret.TouchEvent.TOUCH_TAP, this.onMaskHandler, this);
            a = ResourceUtils.createBitmapByName("reborn_confirm");
            this.addChild(a);
            a.x = 24;
            a.y = 0;
            var b = new SimpleButton,
                c = ResourceUtils.createBitmapFromSheet("close_btn", "commonRes");
            b.addChild(c);
            b.addOnClick(this.onCloseHandler, this);
            b.x = 543;
            b.y = 70;
            b.useZoomOut(!0);
            this.addChild(b);
            this._mainContainer = new egret.DisplayObjectContainer;
            this._mainContainer.x = 24;
            this._mainContainer.y = 110;
            this._mainContainer.width = a.width;
            this.addChild(this._mainContainer);
            this._littleCoin = ResourceUtils.createBitmapFromSheet("reborn_little_icon", "commonRes");
            this._littleCoin.x = 74;
            this._littleCoin.y = 20;
            this._mainContainer.addChild(this._littleCoin);
            this._title = new egret.TextField;
            this._title.text = "\u91cd\u751f";
            this._title.size = 30;
            this._mainContainer.addChild(this._title);
            this._title.x = 124;
            this._title.y = 25;
            this._bigCoin = ResourceUtils.createBitmapFromSheet("reborn_big_icon", "commonRes");
            this._bigCoin.x = 25;
            this._bigCoin.y = 100;
            this._mainContainer.addChild(this._bigCoin);
            this._descLabel = new egret.TextField;
            this._descLabel.text = "\u8fdb\u5165\u91cd\u751f\u6a21\u5f0f\uff0c\u6240\u6709\u8fdb\u7a0b\u5c06\u6e05\u96f6\u5e76\u83b7\u5f97\u9057\u7269\uff0c\u8fd9\u4e9b\u9057\u7269\u5c06\u5e2e\u52a9\u4f60\u66f4\u52a0\u5f3a\u5927\uff0c\u53ea\u8981\u6ee1\u8db3\u6761\u4ef6\u5373\u53ef\u91cd\u751f\uff01";
            this._descLabel.size = 21;
            this._mainContainer.addChild(this._descLabel);
            this._descLabel.x = 225;
            this._descLabel.y = 145;
            this._descLabel.width = 330;
            this._descLabel.lineSpacing = 5;
            this._heroLvTitleLabel = new egret.TextField;
            this._heroLvTitleLabel.text = "\u4f63 \u5175 \u7b49 \u7ea7 \u5956 \u52b1:";
            this._heroLvTitleLabel.size = 21;
            this._mainContainer.addChild(this._heroLvTitleLabel);
            this._heroLvTitleLabel.x = 300 - this._heroLvTitleLabel.width;
            this._heroLvTitleLabel.y = 305;
            this._stageTitleLabel = new egret.TextField;
            this._stageTitleLabel.text = "\u5173 \u5361 \u5b8c \u6210 \u5956 \u52b1:";
            this._stageTitleLabel.size = 21;
            this._mainContainer.addChild(this._stageTitleLabel);
            this._stageTitleLabel.x = 300 - this._stageTitleLabel.width;
            this._stageTitleLabel.y = 335;
            this._teamTitleLabel = new egret.TextField;
            this._teamTitleLabel.text = "\u5168 \u961f \u5b58 \u6d3b \u5956 \u52b1:";
            this._teamTitleLabel.size = 21;
            this._mainContainer.addChild(this._teamTitleLabel);
            this._teamTitleLabel.x = 300 - this._teamTitleLabel.width;
            this._teamTitleLabel.y = 365;
            this._rebornTitleLabel = new egret.TextField;
            this._rebornTitleLabel.text = "\u91cd \u751f \u6280 \u80fd \u5956 \u52b1:";
            this._rebornTitleLabel.size = 21;
            this._mainContainer.addChild(this._rebornTitleLabel);
            this._rebornTitleLabel.x = 300 - this._rebornTitleLabel.width;
            this._rebornTitleLabel.y = 395;
            this._totalTitleLabel = new egret.TextField;
            this._totalTitleLabel.text = "\u5171                    \u8ba1:";
            this._totalTitleLabel.size = 21;
            this._mainContainer.addChild(this._totalTitleLabel);
            this._totalTitleLabel.x = 300 - this._totalTitleLabel.width;
            this._totalTitleLabel.y = 425;
            this._holyCoin1 = ResourceUtils.createBitmapFromSheet("holy_icon", "commonRes");
            this._mainContainer.addChild(this._holyCoin1);
            this._holyCoin1.x = 460 - this._holyCoin1.width;
            this._holyCoin1.y = 295;
            this._heroLvLabel = new egret.TextField;
            this._heroLvLabel.text = "10";
            this._heroLvLabel.size = 18;
            this._mainContainer.addChild(this._heroLvLabel);
            this._heroLvLabel.x = this._holyCoin1.x - this._heroLvLabel.width - 5;
            this._heroLvLabel.y = 305;
            this._heroLvLabel.anchorX = 1;
            this._holyCoin2 = ResourceUtils.createBitmapFromSheet("holy_icon", "commonRes");
            this._mainContainer.addChild(this._holyCoin2);
            this._holyCoin2.x = 460 - this._holyCoin2.width;
            this._holyCoin2.y = 325;
            this._stageLabel = new egret.TextField;
            this._stageLabel.text = "20";
            this._stageLabel.size = 18;
            this._mainContainer.addChild(this._stageLabel);
            this._stageLabel.x = this._holyCoin2.x - this._stageLabel.width - 5;
            this._stageLabel.y = 335;
            this._stageLabel.anchorX = 1;
            this._holyCoin3 = ResourceUtils.createBitmapFromSheet("holy_icon", "commonRes");
            this._mainContainer.addChild(this._holyCoin3);
            this._holyCoin3.x = 460 - this._holyCoin3.width;
            this._holyCoin3.y = 355;
            this._teamLabel = new egret.TextField;
            this._teamLabel.text = "30";
            this._teamLabel.size = 18;
            this._mainContainer.addChild(this._teamLabel);
            this._teamLabel.x = this._holyCoin3.x - this._stageLabel.width - 5;
            this._teamLabel.y = 365;
            this._teamLabel.anchorX = 1;
            this._holyCoin4 = ResourceUtils.createBitmapFromSheet("holy_icon", "commonRes");
            this._mainContainer.addChild(this._holyCoin4);
            this._holyCoin4.x = 460 - this._holyCoin4.width;
            this._holyCoin4.y = 385;
            this._rebornLabel = new egret.TextField;
            this._rebornLabel.text = "40";
            this._rebornLabel.size = 18;
            this._mainContainer.addChild(this._rebornLabel);
            this._rebornLabel.x = this._holyCoin4.x - this._rebornLabel.width - 5;
            this._rebornLabel.y = 395;
            this._rebornLabel.anchorX = 1;
            this._holyCoin5 = ResourceUtils.createBitmapFromSheet("holy_icon", "commonRes");
            this._mainContainer.addChild(this._holyCoin5);
            this._holyCoin5.x = 460 - this._holyCoin5.width;
            this._holyCoin5.y = 415;
            this._holyCoin1.scaleX = this._holyCoin1.scaleY = 0.9;
            this._holyCoin2.scaleX = this._holyCoin2.scaleY = 0.9;
            this._holyCoin3.scaleX = this._holyCoin3.scaleY = 0.9;
            this._holyCoin4.scaleX = this._holyCoin4.scaleY = 0.9;
            this._holyCoin5.scaleX = this._holyCoin5.scaleY = 0.9;
            this._totalLabel = new egret.TextField;
            this._totalLabel.text = "50";
            this._totalLabel.size = 18;
            this._mainContainer.addChild(this._totalLabel);
            this._totalLabel.x = this._holyCoin5.x - this._totalLabel.width - 5;
            this._totalLabel.y = 425;
            this._totalLabel.anchorX = 1;
            this._btnNoContainer = new SimpleButton;
            this._btnNoContainer.anchorX = this._btnNoContainer.anchorY = 0.5;
            this._mainContainer.addChild(this._btnNoContainer);
            this._btnNoContainer.x = 160;
            this._btnNoContainer.y = 500;
            this._btnNoContainer.useZoomOut(!0);
            this._btnNoContainer.addOnClick(this.refuseHandler, this);
            a = ResourceUtils.createBitmapFromSheet("long_red_btn", "commonRes");
            this._btnNoContainer.addChild(a);
            this._btnNoTitle = new egret.TextField;
            this._btnNoTitle.text = "\u53d6 \u6d88";
            this._btnNoTitle.size = 28;
            this._btnNoTitle.textColor = 0;
            this._btnNoTitle.x = Math.floor((this._btnNoContainer.width - this._btnNoTitle.width) / 2);
            this._btnNoTitle.y = 18;
            this._btnNoContainer.addChild(this._btnNoTitle);
            this._btnYesContainer = new SimpleButton;
            this._btnYesContainer.anchorX = this._btnYesContainer.anchorY = 0.5;
            this._mainContainer.addChild(this._btnYesContainer);
            this._btnYesContainer.x = 430;
            this._btnYesContainer.y = 500;
            this._btnYesContainer.useZoomOut(!0);
            this._btnYesContainer.addOnClick(this.onConfirmHandler, this);
            a = ResourceUtils.createBitmapFromSheet("big_long_btn", "commonRes");
            this._btnYesContainer.addChild(a);
            this._btnYesTitle = new egret.TextField;
            this._btnYesTitle.text = "\u91cd \u751f";
            this._btnYesTitle.size = 28;
            this._btnYesTitle.textColor = 0;
            this._btnYesTitle.x = Math.floor((this._btnYesContainer.width - this._btnYesTitle.width) / 2);
            this._btnYesTitle.y = 18;
            this._btnYesContainer.addChild(this._btnYesTitle)
        };
        b.prototype.refreshView = function() {
            var a = HeroController.getInstance().getHeroList(),
                b = 0,
                c = DataCenter.getInstance().totalData.currentLevel,
                e = 0;
            80 < c && (e = Math.floor((1 + Math.floor(c - 80) / 15) * Math.floor((c - 80) / 15) / 2));
            for (var c = 2,
                     h = 0; h < a.length; h++) b += Math.floor(a[h].lv / 1E3),
                a[h].isDead() && (c = 1);
            a = applyFilters(Const.GET_HOLY_EFFECT, getApplyString(19));
            h = 0;
            h = a != getApplyString(19) ? h + a: 0;
            this._heroLvLabel.text = b.toString();
            this._stageLabel.text = e.toString();
            this._teamLabel.text = 2 == c ? "" + (b + e) : "0";
            this._totalLabel.text = Math.floor(c * (b + e) * (1 + h)).toString();
            this._awardNum = Math.floor(c * (b + e) * (1 + h));
            this._rebornLabel.text = 0 == h ? "0": "" + Math.floor(c * (b + e) * h)
        };
        b.prototype.refuseHandler = function() {
            this.onCloseHandler()
        };
        b.prototype.onConfirmHandler = function() {
            DataCenter.getInstance().changeHolyNum(this._awardNum);
            DataCenter.getInstance().resetGame();
            DataCenter.getInstance().statistics.addValue(StatisticsType.rechargeHolyNum, this._awardNum.toString());
            this.onCloseHandler()
        };
        b.prototype.onCloseHandler = function() {
            GameMainLayer.instance.hideResetGameLayer()
        };
        b.prototype.onMaskHandler = function() {};
        return b
    } (BaseContainer);
ResetGameConfirmLayer.prototype.__class__ = "ResetGameConfirmLayer";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    StatisticsLayer = function(c) {
        function b() {
            c.call(this);
            this._currentList = [];
            this._totalList = [];
            this._timeID = -1;
            this.initView()
        }
        __extends(b, c);
        b.prototype.close = function() {
            Time.clearInterval(this._timeID);
            GameMainLayer.instance.hideStatisticsLayer()
        };
        b.prototype.initView = function() {
            var a = ResourceUtils.createBitmapByName("stat_bg");
            a.touchEnabled = !0;
            a.y = 0;
            this.addChild(a);
            var b = new SimpleButton;
            b.addOnClick(this.close, this);
            b.x = 615;
            b.y = 52;
            b.anchorX = b.anchorY = 0.5;
            b.useZoomOut(!0);
            b.addChild(ResourceUtils.createBitmapFromSheet("close_btn"));
            this.addChild(b);
            b = new egret.TextField;
            b.text = "\u7edf \u8ba1 \u6570 \u636e";
            b.x = 235;
            b.y = 52;
            b.size = 38;
            b.textColor = 16766208;
            this.addChild(b);
            this._scrollView = new ScrollView;
            this._scrollView.width = a.width;
            this._scrollView.height = 800;
            this._scrollView.x = 0;
            this._scrollView.y = 250;
            this._scrollView.direction = Direction.VERTICAL;
            this.addChild(this._scrollView);
            this._currentContainer = new egret.DisplayObjectContainer;
            this.addChild(this._currentContainer);
            this._currentTitle = new egret.TextField;
            this._currentTitle.text = "\u5f53\u524d";
            this._currentTitle.x = 20;
            this._currentTitle.y = 0;
            this._currentTitle.size = 34;
            this._currentContainer.addChild(this._currentTitle);
            this._allHeroTitle = new egret.TextField;
            this._allHeroTitle.text = "\u6240\u6709\u82f1\u96c4\u7b49\u7ea7\uff1a";
            this._currentList.push(this._allHeroTitle);
            this._allHeroLabel = new egret.TextField;
            this._allHeroLabel.text = "100";
            this._currentList.push(this._allHeroLabel);
            this._crlRateTitle = new egret.TextField;
            this._crlRateTitle.text = "\u66b4\u51fb\u7387\uff1a";
            this._currentList.push(this._crlRateTitle);
            this._crlRateLabel = new egret.TextField;
            this._crlRateLabel.text = "0.01";
            this._currentList.push(this._crlRateLabel);
            this._dpsAddTitle = new egret.TextField;
            this._dpsAddTitle.text = "DPS\u52a0\u6210";
            this._currentList.push(this._dpsAddTitle);
            this._dpsAddLabel = new egret.TextField;
            this._dpsAddLabel.text = "100";
            this._currentList.push(this._dpsAddLabel);
            this._crlAddTitle = new egret.TextField;
            this._crlAddTitle.text = "\u66b4\u51fb\u52a0\u6210";
            this._currentList.push(this._crlAddTitle);
            this._crlAddLabel = new egret.TextField;
            this._crlAddLabel.text = "100";
            this._currentList.push(this._crlAddLabel);
            this._allscTitle = new egret.TextField;
            this._allscTitle.text = "\u6240\u6709\u91d1\u5e01\u52a0\u6210";
            this._currentList.push(this._allscTitle);
            this._allscLabel = new egret.TextField;
            this._allscLabel.text = "100";
            this._currentList.push(this._allscLabel);
            this._exholyTitle = new egret.TextField;
            this._exholyTitle.text = "\u4e0a\u6b21\u5151\u6362\u7687\u51a0\u540e\u65f6\u95f4";
            this._currentList.push(this._exholyTitle);
            this._exholyLabel = new egret.TextField;
            this._exholyLabel.text = "00:00";
            this._currentList.push(this._exholyLabel);
            this.initContainer(this._currentList, 60, this._currentContainer);
            this._totalTitle = new egret.TextField;
            this._totalTitle.text = "\u7d2f\u8ba1";
            this._totalTitle.x = 20;
            this._totalTitle.y = 320;
            this._totalTitle.size = 34;
            this._currentContainer.addChild(this._totalTitle);
            this._totalScTitle = new egret.TextField;
            this._totalScTitle.text = "\u6240\u6709\u91d1\u5e01\uff1a";
            this._totalList.push(this._totalScTitle);
            this._totalScLabel = new egret.TextField;
            this._totalScLabel.text = "100";
            this._totalList.push(this._totalScLabel);
            this._totalClickTitle = new egret.TextField;
            this._totalClickTitle.text = "\u6240\u6709\u70b9\u51fb\uff1a";
            this._totalList.push(this._totalClickTitle);
            this._totalClickLabel = new egret.TextField;
            this._totalClickLabel.text = "100";
            this._totalList.push(this._totalClickLabel);
            this._killEnemyTitle = new egret.TextField;
            this._killEnemyTitle.text = "\u51fb\u6740\u602a\u517d\u603b\u6570";
            this._totalList.push(this._killEnemyTitle);
            this._killEnemyLabel = new egret.TextField;
            this._killEnemyLabel.text = "100";
            this._totalList.push(this._killEnemyLabel);
            this._killBossTitle = new egret.TextField;
            this._killBossTitle.text = "\u51fb\u6740BOSS\u603b\u6570";
            this._totalList.push(this._killBossTitle);
            this._killBossLabel = new egret.TextField;
            this._killBossLabel.text = "100";
            this._totalList.push(this._killBossLabel);
            this._totalCrlTitle = new egret.TextField;
            this._totalCrlTitle.text = "\u66b4\u51fb\u603b\u6570";
            this._totalList.push(this._totalCrlTitle);
            this._totalCrlLabel = new egret.TextField;
            this._totalCrlLabel.text = "1000";
            this._totalList.push(this._totalCrlLabel);
            this._boxTitle = new egret.TextField;
            this._boxTitle.text = "\u5f00\u542f\u5b9d\u7bb1";
            this._totalList.push(this._boxTitle);
            this._boxLabel = new egret.TextField;
            this._boxLabel.text = "100";
            this._totalList.push(this._boxLabel);
            this._maxLvTitle = new egret.TextField;
            this._maxLvTitle.text = "\u6700\u9ad8\u7b49\u7ea7";
            this._totalList.push(this._maxLvTitle);
            this._maxLvLabel = new egret.TextField;
            this._maxLvLabel.text = "111";
            this._totalList.push(this._maxLvLabel);
            this._totalExholyTitle = new egret.TextField;
            this._totalExholyTitle.text = "\u5171\u8ba1\u5151\u6362\u7687\u51a0";
            this._totalList.push(this._totalExholyTitle);
            this._totalExholyLabel = new egret.TextField;
            this._totalExholyLabel.text = "100";
            this._totalList.push(this._totalExholyLabel);
            this._exholyLostScTitle = new egret.TextField;
            this._exholyLostScTitle.text = "\u5151\u6362\u7687\u51a0\u5931\u53bb\u7684\u91d1\u5e01\u603b\u6570";
            this._totalList.push(this._exholyLostScTitle);
            this._exholyLostScLabel = new egret.TextField;
            this._exholyLostScLabel.text = "100";
            this._totalList.push(this._exholyLostScLabel);
            this._exholyLostDpsTitle = new egret.TextField;
            this._exholyLostDpsTitle.text = "\u5151\u6362\u7687\u51a0\u5931\u53bb\u7684DPS\u603b\u6570";
            this._totalList.push(this._exholyLostDpsTitle);
            this._exholyLostDpsLabel = new egret.TextField;
            this._exholyLostDpsLabel.text = "100";
            this._totalList.push(this._exholyLostDpsLabel);
            this._playDayTitle = new egret.TextField;
            this._playDayTitle.text = "\u6e38\u620f\u5929\u6570";
            this._totalList.push(this._playDayTitle);
            this._playDayLabel = new egret.TextField;
            this._playDayLabel.text = "100";
            this._totalList.push(this._playDayLabel);
            this._playTimeTitle = new egret.TextField;
            this._playTimeTitle.text = "\u6e38\u620f\u65f6\u95f4";
            this._totalList.push(this._playTimeTitle);
            this._playTimeLabel = new egret.TextField;
            this._playTimeLabel.text = "48:00:21";
            this._totalList.push(this._playTimeLabel);
            this._shengwuTitle = new egret.TextField;
            this._shengwuTitle.text = "\u5723\u7269";
            this._totalList.push(this._shengwuTitle);
            this._shengwuLabel = new egret.TextField;
            this._shengwuLabel.text = "100";
            this._totalList.push(this._shengwuLabel);
            this._girlTitle = new egret.TextField;
            this._girlTitle.text = "\u6469\u6469\u516c\u4e3b";
            this._totalList.push(this._girlTitle);
            this._girlLabel = new egret.TextField;
            this._girlLabel.text = "100";
            this._totalList.push(this._girlLabel);
            this.initContainer(this._totalList, 380, this._currentContainer);
            this._currentContainer.cacheAsBitmap = !0;
            this._scrollView.setContainer(this._currentContainer, a.width, 1050);
            this._idLabel = new egret.TextField;
            this._idLabel.x = 80;
            this._idLabel.y = 130;
            this._idLabel.size = 28;
            this.addChild(this._idLabel);
            this._idLabel.text = "\u73a9\u5bb6ID:" + ProxyUtils.userId
        };
        b.prototype.initContainer = function(a, b, c) {
            for (var e in a) {
                var h = Math.floor(e / 2);
                1 == e % 2 ? (a[e].x = 610, a[e].anchorX = 1, a[e].size = 28, a[e].textColor = 14447689) : (a[e].x = 20, a[e].size = 28, a[e].textColor = 16777215);
                a[e].y = b + 30 * h + 10 * h;
                c.addChild(a[e])
            }
        };
        b.prototype.createLabelContainer = function() {
            this._labelContainer = new egret.DisplayObjectContainer
        };
        b.prototype.refreshView = function() {
            this._allHeroLabel.text = this.getTotalHeroLevel().toString();
            this._crlRateLabel.text = this.getCritPro();
            this._dpsAddLabel.text = this.getDPSAdd();
            this._crlAddLabel.text = this.getCritAdd();
            this._allscLabel.text = this.getGoldAdd();
            this._exholyLabel.text = this.getLastReborn();
            this._totalScLabel.text = this.getTotalSc();
            this._totalClickLabel.text = this.getClickNum().toString();
            this._killEnemyLabel.text = this.killMonsterNum().toString();
            this._killBossLabel.text = this.killBossNum().toString();
            this._totalCrlLabel.text = this.getCritNum().toString();
            this._boxLabel.text = this.getBoxNum().toString();
            this._maxLvLabel.text = this.getMaxLv().toString();
            this._totalExholyLabel.text = this.getTotalRechargeHolyNum().toString();
            this._exholyLostScLabel.text = this.getLoseGoldNum().toString();
            this._exholyLostDpsLabel.text = this.getLoseDPS().toString();
            this._playDayLabel.text = this.getGameDay();
            this._playTimeLabel.text = this.getOnlineTime();
            this._shengwuLabel.text = this.getHolyNum().toString();
            this._girlLabel.text = this.getGiftNum().toString();
            this._timeID = Time.setInterval(this.refreshTime, this, 1E3)
        };
        b.prototype.refreshTime = function() {
            this._exholyLabel.text = this.getLastReborn();
            this._playTimeLabel.text = this.getOnlineTime()
        };
        b.prototype.getTotalHeroLevel = function() {
            var a = DataCenter.getInstance().totalData.heroList,
                b = 0,
                c;
            for (c in a) b += a[c].lv;
            return b
        };
        b.prototype.getCritPro = function() {
            return 100 * HeroController.getInstance().mainHero.critPro + "%"
        };
        b.prototype.getDPSAdd = function() {
            for (var a = HeroController.getInstance().mainHero, a = SkillController.getInstance().getAdditionByHeroModel(a), b = 0, c = 0; c < a.length; c++) 4 == a[c].getEffectType() && (b += a[c].getValue());
            return 100 * b + "%"
        };
        b.prototype.getCritAdd = function() {
            return 100 * HeroController.getInstance().mainHero.critHurt + "%"
        };
        b.prototype.getGoldAdd = function() {
            return (100 * (SkillController.getInstance().dropCoinTimes * SkillController.getInstance().dropCoinTimes1 - 1)).toFixed(0) + "%"
        };
        b.prototype.getLastReborn = function() {
            var a = DataCenter.getInstance().statistics.getDataByType(StatisticsType.rebornTime);
            if (0 == a) return "0";
            var b = (new Date).getTime();
            return GameUtils.formatTime1(Math.floor((b - a) / 1E3))
        };
        b.prototype.getTotalSc = function() {
            return DataCenter.getInstance().getScNum()
        };
        b.prototype.getClickNum = function() {
            return DataCenter.getInstance().statistics.getValue(StatisticsType.tap)
        };
        b.prototype.killMonsterNum = function() {
            return DataCenter.getInstance().statistics.getValue(StatisticsType.enemy)
        };
        b.prototype.killBossNum = function() {
            return DataCenter.getInstance().statistics.getValue(StatisticsType.boss)
        };
        b.prototype.getCritNum = function() {
            return DataCenter.getInstance().statistics.getValue(StatisticsType.crit)
        };
        b.prototype.getBoxNum = function() {
            return DataCenter.getInstance().statistics.getValue(StatisticsType.openbox)
        };
        b.prototype.getMaxLv = function() {
            var a = DataCenter.getInstance().statistics.getValue(StatisticsType.maxLv);
            if ("0" == a) return "1";
            var b = new NumberModel;
            b.add(a);
            return b.getData()
        };
        b.prototype.getTotalRechargeHolyNum = function() {
            return DataCenter.getInstance().statistics.getValue(StatisticsType.rechargeHolyNum)
        };
        b.prototype.getLoseGoldNum = function() {
            return DataCenter.getInstance().statistics.getValue(StatisticsType.loseSc)
        };
        b.prototype.getLoseDPS = function() {
            return DataCenter.getInstance().statistics.getValue(StatisticsType.loseDPS)
        };
        b.prototype.getGameDay = function() {
            var a = DataCenter.getInstance().totalData.gameTime;
            return GameUtils.formatDay(a)
        };
        b.prototype.getOnlineTime = function() {
            var a = DataCenter.getInstance().statistics.getValue(StatisticsType.onLineTime);
            return GameUtils.formatTime1(parseInt(a))
        };
        b.prototype.getHolyNum = function() {
            return DataCenter.getInstance().getHolyNum()
        };
        b.prototype.getGiftNum = function() {
            return DataCenter.getInstance().statistics.getValue(StatisticsType.gift)
        };
        return b
    } (BaseContainer);
StatisticsLayer.prototype.__class__ = "StatisticsLayer";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    SupriseLayer = function(c) {
        function b() {
            c.call(this);
            this.initView()
        }
        __extends(b, c);
        b.prototype.initView = function() {
            this._container = new egret.DisplayObjectContainer;
            var a = ResourceUtils.createBitmapByName("mask");
            this._container.addChild(a);
            a.touchEnabled = !0;
            a = egret.DynamicBitmap.create("suprise_bg");
            a.x = 18;
            this._container.addChild(a);
            this.addChild(this._container);
            a = new egret.TextField;
            a.text = "\u606d\u559c\u60a8\u83b7\u5f97 \u201c\u7545\u68a6\u6e38\u620f\u201d \u65b0\u6625\u5927\u793c\u5305";
            a.size = 26;
            a.x = 60;
            a.y = 300;
            this._container.addChild(a);
            this._label1 = new egret.TextField;
            this._label1.textFlow = (new egret.HtmlTextParser).parser('\u5173\u6ce8\u5fae\u4fe1\u53f7:<font color="0xff0000">changmengyouxi</font>');
            this._label1.size = 26;
            this._label1.x = 60;
            this._label1.y = 350;
            this._container.addChild(this._label1);
            this._label2 = new egret.TextField;
            this._label2.size = 26;
            this._label2.x = 60;
            this._label2.y = 400;
            this._container.addChild(this._label2);
            this._label3 = new egret.TextField;
            this._label3.size = 26;
            this._label3.x = 60;
            this._label3.y = 450;
            this._container.addChild(this._label3);
            this._label3.text = "\u9886\u53d6\u65b9\u5f0f\u975e\u5e38\u7b80\u5355\uff1a";
            a = new egret.TextField;
            a.size = 22;
            a.text = "1\u3001\u9a6c\u4e0a\u5c06\u672c\u5c4f\u5e55\u622a\u56fe\n2\u3001\u5173\u6ce8\u6211\u4eec\u5fae\u4fe1\u516c\u4f17\u53f7\uff0c\u5e76\u7ed9\u6211\u4eec\u53d1\u9001\u201c\u65b0\u6625\u5927\u793c\u5305\u201d\n\u5c31\u53ef\u4ee5\u5566\uff0c\u7b80\u5355\u5427~";
            a.x = 60;
            a.lineSpacing = 10;
            a.y = 500;
            this._container.addChild(a);
            this._container.cacheAsBitmap = !0;
            var a = new SimpleButton,
                b = ResourceUtils.createBitmapFromSheet("close_btn", "commonRes");
            a.addChild(b);
            a.addOnClick(this.onClickHandler, this);
            a.x = 543;
            a.y = 170;
            a.useZoomOut(!0);
            this.addChild(a);
            this.addChild(GameUtils.createButton("\u53bb\u9886\u5956\u5566", "unlock_skill_btn", this.gotoAward, 165, 650, 171, 69, 0, -5, 0));
            this.addChild(GameUtils.createButton("\u5206\u4eab\u7ed9\u670b\u53cb", "share_btn_2", this.shareFriend, 440, 650, 250, 83, 32, -3))
        };
        b.prototype.gotoAward = function() {
            GameUtils.notEmpty(ProxyUtils.boxUrl) && (location.href = ProxyUtils.boxUrl)
        };
        b.prototype.shareFriend = function() {
            doAction(Const.AWARD_BOX_TIP)
        };
        b.prototype.onClickHandler = function() {
            GameMainLayer.instance.hideSupriseLayer()
        };
        b.prototype.refreshView = function(a) {
            this._label1.textFlow = (new egret.HtmlTextParser).parser('\u5956\u54c1\uff1a<font color="0xff0000">' + ProxyUtils.awdTo.awddes + "</font>");
            this._label2.textFlow = (new egret.HtmlTextParser).parser('\u5151\u6362\u7801\uff1a<font color="0xff0000">' + ProxyUtils.awdTo.code + "</font>")
        };
        return b
    } (BaseContainer);
SupriseLayer.prototype.__class__ = "SupriseLayer";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    TipsContainer = function(c) {
        function b() {
            c.call(this)
        }
        __extends(b, c);
        b.prototype.showMessage = function(a) {
            for (var b = 0; b < this.numChildren; b++) this.getChildAt(b).y -= 50;
            b = new TipsLabel;
            b.showAnimation(a);
            this.addChild(b)
        };
        return b
    } (BaseContainer);
TipsContainer.prototype.__class__ = "TipsContainer";
var TipsLabel = function(c) {
    function b() {
        c.call(this);
        this.anchorX = 0.5
    }
    __extends(b, c);
    b.prototype.showAnimation = function(a) {
        var b = this;
        this.x = 0;
        var c = ResourceUtils.createBitmapByName("share_input_bg");
        c.scaleX = 0.8;
        c.scaleY = 0.8;
        this.addChild(c);
        c = new egret.TextField;
        c.anchorX = 0.5;
        c.x = 188;
        c.y = 10;
        this.addChild(c);
        c.text = a;
        egret.Tween.get(this).to({
                x: 320
            },
            200, egret.Ease.backOut).wait(5E3).call(function() {
                GameUtils.removeFromParent(b)
            })
    };
    return b
} (egret.DisplayObjectContainer);
TipsLabel.prototype.__class__ = "TipsLabel";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    TopListLayer = function(c) {
        function b() {
            c.call(this);
            this.init()
        }
        __extends(b, c);
        b.prototype.close = function() {
            GameMainLayer.instance.hideTopListLayer()
        };
        b.prototype.init = function() {
            var a = ResourceUtils.createBitmapByName("mask");
            this.addChild(a);
            a = ResourceUtils.createBitmapByName("achieve_bg");
            a.touchEnabled = !0;
            a.y = 0;
            this.addChild(a);
            a = new SimpleButton;
            a.addOnClick(this.close, this);
            a.x = 598;
            a.y = 55;
            a.anchorX = a.anchorY = 0.5;
            a.useZoomOut(!0);
            a.addChild(ResourceUtils.createBitmapFromSheet("close_btn"));
            this.addChild(a);
            this._infoContainer = new egret.DisplayObjectContainer;
            this.addChild(this._infoContainer);
            a = new egret.TextField;
            a.text = "\u6392 \u884c \u699c";
            a.size = 35;
            a.x = 325;
            a.y = 65;
            a.textColor = 0;
            a.anchorX = a.anchorY = 0.5;
            this._infoContainer.addChild(a);
            a = new egret.TextField;
            a.text = "\u95ef\u5173\u6700\u9ad8\u7684\u524d10\u540d";
            a.textColor = 16777215;
            a.size = 25;
            a.x = 60;
            a.y = 130;
            this._infoContainer.addChild(a);
            this._refreshBtn = new SimpleButton;
            this._refreshBtn.x = 520;
            this._refreshBtn.y = 114;
            this._refreshBtn.scaleX = this._refreshBtn.scaleY = 0.7;
            this._refreshBtn.anchorX = this._refreshBtn.anchorX = 0.5;
            this._refreshBtn.useZoomOut(!0);
            this._refreshBtn.addOnClick(this.reload, this);
            a = ResourceUtils.createBitmapFromSheet("hero_lvup_btn", "commonRes");
            this._refreshBtn.addChild(a);
            a = new egret.TextField;
            a.text = "\u5237  \u65b0";
            a.x = 45;
            a.y = 15;
            a.textColor = 0;
            this._refreshBtn.addChild(a);
            this.addChild(this._refreshBtn);
            this._selfItem = new TopItemLayer("self");
            this._selfItem.x = 13;
            this._selfItem.y = 205;
            this._splitSpr = ResourceUtils.createBitmapFromSheet("item_split", "commonRes");
            this._splitSpr.x = 5;
            this._splitSpr.y = 315;
            this.addChild(this._selfItem);
            this.addChild(this._splitSpr);
            this._topTable = new TableView;
            this._topTable.y = 340;
            this._topTable.x = 13;
            this.addChild(this._topTable);
            this._topTable.width = GameUtils.getWinWidth();
            this._topTable.height = 600;
            this._topTable.setList(DataCenter.getInstance().topList, Direction.VERTICAL, this, GameUtils.getWinWidth(), 110)
        };
        b.prototype.reload = function() {
            var a = {
                pic: ""
            };
            a.name = ProxyUtils.nickName ? ProxyUtils.nickName: "\u5c0f\u6469\u6469";
            a.top = DataCenter.getInstance().top;
            a.life = 0;
            this._selfItem.refreshView(a, -1);
            a = new SingleProxy({
                mod: "Top",
                "do": "getList",
                p: {}
            });
            a.addEventListener(ProxyEvent.RESPONSE_SUCCEED, this.refreshView, this);
            a.load()
        };
        b.prototype.refreshView = function(a) {
            DataCenter.getInstance().topList = a.responseData.l;
            var b = {};
            b.pic = a.responseData.i.pic;
            b.name = ProxyUtils.nickName ? ProxyUtils.nickName: "\u5c0f\u6469\u6469";
            b.top = a.responseData.i.top;
            b.life = a.responseData.i.life;
            this._selfItem.refreshView(b, -1);
            this._topTable.reloadData(DataCenter.getInstance().topList)
        };
        b.prototype.createItemRenderer = function() {
            return new TopItemLayer
        };
        b.prototype.updateItemRenderer = function(a, b, c) {
            a.refreshView(b, c)
        };
        b.prototype.dispose = function() {
            c.prototype.dispose.call(this);
            removeEventsByTarget(this)
        };
        return b
    } (BaseContainer);
TopListLayer.prototype.__class__ = "TopListLayer";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    AdLayer = function(c) {
        function b() {
            c.call(this);
            this._d = this._conf = null;
            this.init()
        }
        __extends(b, c);
        b.prototype.init = function() {
            this._container = new egret.DisplayObjectContainer;
            this.addChild(this._container);
            var a = ResourceUtils.createBitmapByName("mask");
            this._container.addChild(a);
            a.touchEnabled = !0;
            a = new SimpleButton;
            a.anchorX = a.anchorY = 0.5;
            var b = ResourceUtils.createBitmapFromSheet("close_btn");
            a.addChild(b);
            a.addOnClick(this.close, this);
            a.useZoomOut(!0);
            a.x = 560;
            a.y = 80;
            this.addChild(a)
        };
        b.prototype.close = function() {
            GameMainLayer.instance.hideAdLayer()
        };
        b.prototype.show = function(a) {
            this._conf = a;
            GameUtils.removeFromParent(this._d);
            this._d = egret.DynamicBitmap.create("ad_" + GameUtils.random(1, 5) + "_2g");
            this._d.anchorX = this._d.anchorY = 0.5;
            this._d.x = 320;
            this._d.y = 480;
            this._container.addChild(this._d);
            DisplayTouchHandler.create(this._d, this.clickad, this)
        };
        b.prototype.clickad = function() {
            GameMainLayer.instance.hideAdLayer();
            this.showUrl()
        };
        b.prototype.showVideo = function() {
            var a = this,
                b = document.createElement("DIV");
            b.style.zIndex = "1000";
            b.style.width = "100%";
            b.style.height = "100%";
            b.style.position = "absolute";
            b.style.left = "0px";
            b.style.top = "0px";
            b.style.overflow = "scroll";
            b.style.backgroundColor = "#aaaaaa";
            document.body.appendChild(b);
            var c = document.createElement("VIDEO");
            c.style.width = "100%";
            c.style.position = "absolute";
            c.style.top = "30%";
            c.style.left = "0";
            c.src = "resource/assets/ad/ad.mp4";
            c.onended = function() {
                var c = document.createElement("DIV");
                c.style.position = "absolute";
                c.style.zIndex = "10000";
                c.style.right = "20px";
                c.style.top = "30%";
                c.onclick = function() {
                    document.body.removeChild(b)
                };
                var g = document.createElement("IMG");
                g.src = "resource/assets/ad_close.png";
                g.width = 60;
                g.height = 60;
                c.appendChild(g);
                b.appendChild(c);
                delete a._conf.ad;
                doAction(Const.SHOW_AD_DONE, a._conf)
            };
            c.autoplay = !0;
            c.controls = !0;
            b.appendChild(c)
        };
        b.prototype.showUrl = function() {
            var a = this,
                b = document.createElement("DIV");
            b.style.zIndex = "1000";
            b.style.position = "absolute";
            b.style.left = "0px";
            b.style.top = "0px";
            b.style.overflow = "scroll";
            b.style.width = "640px";
            b.style.height = "1235px";
            document.body.appendChild(b);
            b.style["-webkit-transform-origin"] = "0% 0% 0px";
            b.style.transform = "scale(" + window.innerWidth / 640 + ")";
            b.style["-webkit-transform"] = "scale(" + window.innerWidth / 640 + ")";
            b.style.height = 640 * window.innerHeight / window.innerWidth + "px";
            var c = document.createElement("IFRAME");
            c.src = "http://mole.51.com/";
            c.width = "640px";
            c.height = "1235px";
            c.onload = function() {
                var c = document.createElement("DIV");
                c.style.position = "absolute";
                c.style.zIndex = "10000";
                c.style.right = "20px";
                c.style.top = "20px";
                c.onclick = function() {
                    document.body.removeChild(b)
                };
                var g = document.createElement("IMG");
                g.src = "resource/assets/ad_close.png";
                c.appendChild(g);
                b.appendChild(c);
                delete a._conf.ad;
                doAction(Const.SHOW_AD_DONE, a._conf)
            };
            b.appendChild(c)
        };
        return b
    } (BaseContainer);
AdLayer.prototype.__class__ = "AdLayer";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    GameLayer = function(c) {
        function b() {
            c.call(this);
            this._bossTime = this._tickTime = 30;
            this._skillAnimationIdx = 1;
            this._skillTimeID = -1;
            this._skillTickNum = 0;
            this._mv = null;
            this.init()
        }
        __extends(b, c);
        b.prototype.init = function() {
            var a = this;
            this.refreshHome();
            this.initBossTimer();
            this.initTouchArea();
            this.initTickText();
            this.initBossStatus();
            this.initHero();
            this.initOfflineView();
            this.initCoinView();
            this.initSetButton();
            this.initElfView();
            this.initBoxEffect();
            addAction(Const.REFRESH_BOSS, this.refreshBoss, this, 1);
            addAction(Const.REFRESH_HOME_BG, this.refreshHome, this);
            addAction(Const.ADD_HERO, this.addHero, this);
            addAction(Const.REBORN_HERO, this.addHero1, this);
            addAction(Const.LEAVE_BATTLE, this.leaveBattle, this);
            addAction(Const.FIGHT_BOSS, this.fightBoss, this);
            addAction(Const.BOSS_KILLED, this.bossKilled, this);
            addAction(Const.HERO_ATTACK, this.heroAttack, this);
            addAction(Const.BEGIN_MAIN_SKILL_1, this.beginMainSkill1, this);
            addAction(Const.BEGIN_MAIN_SKILL_2, this.beginMainSkill2, this);
            addAction(Const.BEGIN_MAIN_SKILL_3, this.beginMainSkill3, this);
            addAction(Const.BEGIN_MAIN_SKILL_4, this.beginMainSkill4, this);
            addAction(Const.BEGIN_MAIN_SKILL_5, this.beginMainSkill5, this);
            addAction(Const.STOP_MAIN_SKILL_2, this.stopMainSkill2, this);
            addAction(Const.STOP_MAIN_SKILL_VALUE_2, this.stopMainSkillValue, this);
            addAction(Const.STOP_MAIN_SKILL_3, this.stopAnimation3, this);
            addAction(Const.STOP_MAIN_SKILL_4, this.stopAnimation4, this);
            addAction(Const.STOP_MAIN_SKILL_5, this.stopAnimation5, this);
            addAction(Const.SHOWBOXDESC, this.showOpenBoxEffect, this);
            addAction(Const.RESET_GAME, this.resetGame, this);
            addAction(Const.FLY_COIN, this.flyCoin, this, 99);
            addFilter(Const.MAIN_LAYER, this.mainLayer, this);
            addAction(Const.USE_PAYMENT_SKILL, this.usePaymentSkill, this);
            addAction(Const.HERO_UPGRADE, this.heroUpgradeHandler, this);
            addAction("lala",
                function(b) {
                    a.addHurtText(b)
                },
                this)
        };
        b.prototype.usePaymentSkill = function(a) {
            var b = this;
            "blood" == a && GameUtils.preloadAnimation("hero_skill_6_json",
                function() {
                    var a = new Movie("hero_skill_6_json", "hero_skill_6");
                    b.addChild(a);
                    a.play("1");
                    a.setCallback(function(a) {
                            "attack" == a && ((new ShakeAnimation(100, 10, 5)).run(this._bg), a = applyFilters(Const.GET_PAYMENT_EFFECT), this._boss.attacked(a.dmg) && this.addHurtText(a))
                        },
                        b);
                    a.x = 320;
                    a.y = 500
                })
        };
        b.prototype.mainLayer = function() {
            return this
        };
        b.prototype.resetGame = function() {
            this.refreshHome();
            this.refreshBoss();
            this._fightStatusBtn.setStatus(3);
            this._heroContainer.removeChildren();
            this.initHero();
            this._tickID && Time.clearInterval(this._tickID);
            this._tickTxt.visible = !1;
            this._bossTimeBar.visible = !1
        };
        b.prototype.flyCoin = function(a) {
            var b = this,
                c = (new Date).getTime(),
                e = a.parent.localToGlobal(a.x, a.y),
                e = this.globalToLocal(e.x, e.y, e);
            GameUtils.preloadAnimation("coin_star_json",
                function() {
                    if (2E3 > (new Date).getTime() - c) {
                        var a = new Movie("coin_star_json", "coin_star");
                        a.play("1");
                        a.x = e.x;
                        a.y = e.y;
                        b.addChild(a)
                    }
                })
        };
        b.prototype.initBossTimer = function() {
            this._bossTimeBar = ResourceUtils.createBitmapFromSheet("boss_tick_progress", "commonRes");
            this._bossTimeBar.anchorX = 0;
            this._bossTimeBar.anchorY = 0.5;
            this._bossTimeBar.x = 187;
            this._bossTimeBar.y = 119;
            this._bossTimeBar.visible = !1;
            this.addChild(this._bossTimeBar)
        };
        b.prototype.initOfflineView = function() {
            var a = (new Date).getTime(),
                b = DataCenter.getInstance().currentTime;
            DataCenter.getInstance().currentTime = a;
            a = Math.floor((a - b) / 1E3);
            a = Math.min(a, 14400);
            // 离线结算 
            if (0 <= a) {
                var c = HeroController.getInstance().currentDPS,
                    b = new NumberModel;
                b.add(c);
                b.times(a / 65);
                c = applyFilters(Const.GET_HOLY_EFFECT, getApplyString(29));
                c != getApplyString(29) && b.times(c + 1);
                "0" != b.getNumer() && (this._offlineView = new OfflineView(a, b), this._offlineView.x = -40, this._offlineView.y = 200, egret.Tween.get(this._offlineView).to({
                        x: 55
                    },
                    500, egret.Ease.circInOut), this.addChild(this._offlineView))
            }
        };
        b.prototype.initElfView = function() {
            this._elfView = new ElfView;
            this.addChild(this._elfView)
        };
        b.prototype.initBoxEffect = function() {
            this._boxEffectContainer = new egret.DisplayObjectContainer;
            this._boxEffectContainer.x = 200;
            this._boxEffectContainer.y = 510;
            var a = ResourceUtils.createBitmapFromSheet("box_skill_bg", "commonRes");
            this._boxEffectContainer.addChild(a);
            this._boxIconContainer = new egret.DisplayObjectContainer;
            this._boxEffectContainer.addChild(this._boxIconContainer);
            this._boxIconContainer.y = 10;
            this._boxLabel = new egret.TextField;
            this._boxLabel.text = "\u4f60\u91ca\u653e\u4e86\u4e00\u4e2a\u6280\u80fd";
            this._boxLabel.size = 18;
            this._boxLabel.y = 8;
            this._boxEffectContainer.addChild(this._boxLabel);
            this._boxTimeLabel = new egret.TextField;
            this._boxTimeLabel.text = "\u4f60\u91ca\u653e\u4e86\u4e00\u4e2a\u6280\u80fd";
            this._boxTimeLabel.size = 18;
            this._boxTimeLabel.y = 8;
            this._boxTimeLabel.x = this._boxLabel.width + 30;
            this._boxEffectContainer.addChild(this._boxTimeLabel);
            this._boxEffectContainer.visible = !1;
            this.addChild(this._boxEffectContainer)
        };
        b.prototype.showOpenBoxEffect = function(a) {
            var b, c;
            this._boxIconContainer.removeChildren();
            var e;
            a.id ? (c = a.id % 10, b = ResourceUtils.createBitmapFromSheet("skill_icon_" + c, "commonRes"), c = HeroController.getInstance().mainHero.skillList[c - 1].getDes(), b.scaleX = b.scaleY = 0.5, b.y = -15, e = 0.5) : 1 == a.type ? (b = ResourceUtils.createBitmapFromSheet("coin_icon", "commonRes"), b.scaleX = b.scaleY = 0.75, b.y = -8, c = "\u83b7\u5f97\u4e86 " + a.total + " \u91d1\u5e01", e = 0.75) : 2 == a.type && (b = ResourceUtils.createBitmapFromSheet("gold_icon", "commonRes"), b.scaleX = b.scaleY = 0.75, b.y = -8, c = "\u83b7\u5f97\u4e86" + a.one + "\u94bb\u77f3", e = 0.75);
            this._boxIconContainer.addChild(b);
            this._boxLabel.text = c;
            this._boxLabel.x = b.width * e + 20;
            this._boxEffectContainer.visible = !0;
            if (a.id) this._boxTimeLabel.x = this._boxLabel.x + this._boxLabel.width + 20,
                this._boxTimeLabel.text = "00:15",
                this._boxTimeLabel.visible = !0,
                Time.setTimeout(this.updateBoxTime, this, 1E3, 14);
            else {
                this._boxTimeLabel.visible = !1;
                var h = this;
                Time.setTimeout(function() {
                        h._boxEffectContainer.visible = !1
                    },
                    this, 2E3)
            }
        };
        b.prototype.updateBoxTime = function(a) {
            this._boxTimeLabel.text = 9 < a ? "00:" + a: "00:0" + a;
            0 < a ? (a--, Time.setTimeout(this.updateBoxTime, this, 1E3, a)) : this._boxEffectContainer.visible = !1
        };
        b.prototype.beginMainSkill1 = function(a) {
            GameUtils.preloadAnimation("hero_skill_1_json", this.showAnimation1, this, a)
        };
        b.prototype.showAnimation1 = function(a) {
            var b = new Movie("hero_skill_1_json", "hero_skill_1");
            b.play("1");
            b.x = 320;
            b.y = 530;
            b.setCallback(function(b) {
                    "attack" == b && (SoundManager.getInstance().playEffect("swingSound"), b = DataCenter.getInstance().getTheDamage(a), (new ShakeAnimation(100, 20, 10)).run(this._bg), this._boss.attacked(b.dmg), this._mainHero.attack(), this.addHurtText(b))
                },
                this);
            this.addChild(b)
        };
        b.prototype.showAnimation2 = function() {
            this._skillAnimationIdx = 1;
            this._skillAnimation = new Movie("hero_skill_2_json", "hero_skill_2");
            this._skillAnimation.play("1");
            this._skillAnimation.x = 320;
            this._skillAnimation.y = 530;
            this._skillAnimation.autoRemove = !1;
            this._skillAnimation.setCallback(this.onMoveCallback, this);
            this.addChild(this._skillAnimation)
        };
        b.prototype.showAnimation3 = function() {
            this._skillAnimation3 = new Movie("hero_skill_3_json", "hero_skill_3");
            this._skillAnimation3.play("1");
            this._skillAnimation3.x = 320;
            this._skillAnimation3.y = 530;
            this.addChild(this._skillAnimation3)
        };
        b.prototype.showAnimation4 = function() {
            this._skillAnimation4 = new Movie("hero_skill_4_json", "hero_skill_4");
            this._skillAnimation4.play("1");
            this._skillAnimation4.x = 310;
            this._skillAnimation4.y = 530;
            this.addChild(this._skillAnimation4)
        };
        b.prototype.beginMainSkill3 = function() {
            GameUtils.preloadAnimation("hero_skill_3_json", this.showAnimation3, this)
        };
        b.prototype.beginMainSkill4 = function() {
            GameUtils.preloadAnimation("hero_skill_4_json", this.showAnimation4, this)
        };
        b.prototype.beginMainSkill5 = function() {
            RES.hasRes("hero_skill_5_json") && (this._skillAnimation5 = new Movie("hero_skill_5_json", "hero_skill_5"), this._skillAnimation5.play("1"), this._skillAnimation5.x = 320, this._skillAnimation5.y = 530, this.addChild(this._skillAnimation5))
        };
        b.prototype.stopAnimation3 = function() {
            this._skillAnimation3 && (this.removeChild(this._skillAnimation3), this._skillAnimation3.dispose(), this._skillAnimation3 = null)
        };
        b.prototype.stopAnimation4 = function() {
            this._skillAnimation4 && (this.removeChild(this._skillAnimation4), this._skillAnimation4.dispose(), this._skillAnimation4 = null)
        };
        b.prototype.stopAnimation5 = function() {
            this._skillAnimation5 && (this.removeChild(this._skillAnimation5), this._skillAnimation5.dispose(), this._skillAnimation5 = null)
        };
        b.prototype.stopAnimation2 = function() {
            this._skillAnimationIdx = 3;
            this._skillAnimation && this._skillAnimation.play("3")
        };
        b.prototype.onMoveCallback = function(a, b) {
            a == Movie.PLAY_COMPLETE && (1 == this._skillAnimationIdx && (this._skillAnimationIdx = 2, this._skillAnimation.play("2")), 3 == this._skillAnimationIdx && (this.removeChild(this._skillAnimation), this._skillAnimation.dispose(), this._skillAnimation = null))
        };
        b.prototype.beginMainSkill2 = function(a) {
            null == this._skillAnimation && GameUtils.preloadAnimation("hero_skill_2_json", this.showAnimation2, this);
            this._tickSplit = Math.ceil(a / 10);
            4 <= this._tickSplit && (this._tickSplit = 3);
            this._tickNum = 0;
            this._tickTimeSkill2 = Time.setInterval(this.autoHit, this, 1E3 / a)
        };
        b.prototype.autoHit = function() {
            this._tickNum += 1;
            SoundManager.getInstance().playEffect("swingSound");
            var a = DataCenter.getInstance().getAttackInfo();
            a.crit && (new ShakeAnimation(100, 10, 5)).run(this._bg);
            0 == this._tickNum % this._tickSplit ? this._boss.attacked(a.dmg) && this.addHurtText(a) : this._boss.attackedWithoutAction(a.dmg)
        };
        b.prototype.stopMainSkill2 = function() {
            this.stopAnimation2();
            Time.clearInterval(this._tickTimeSkill2)
        };
        b.prototype.stopMainSkillValue = function() {
            Time.clearInterval(this._tickTimeSkill2)
        };
        b.prototype.initTickText = function() {
            this._tickTxt = new egret.BitmapText;
            var a = RES.getRes("boss_countdown_fnt_fnt");
            this._tickTxt.font = a;
            this.addChild(this._tickTxt);
            this._tickTxt.x = 120;
            this._tickTxt.y = 97;
            this._tickTxt.text = "10.00";
            this._tickTxt.visible = !1
        };
        b.prototype.heroAttack = function(a) {
            this._boss.attackedWithoutAction(a)
        };
        b.prototype.initBossStatus = function() {
            GameUtils.isBoss() ? (this.initBoss(!1), this.leaveBattle()) : (this.initBoss(), this._fightStatusBtn.setStatus(3), this._lvView.showLevel())
        };
        b.prototype.leaveBattle = function() {
            this._fightStatusBtn.setStatus(1);
            this._boss.refreshBoss(!1);
            this._lvView.hideLevel();
            this._tickID && Time.clearInterval(this._tickID);
            this._tickTxt.visible = !1;
            this._bossTimeBar.visible = !1;
            DataCenter.getInstance().showDieLayer()
        };
        b.prototype.fightBoss = function() {
            this._fightStatusBtn.setStatus(2);
            this._lvView.hideLevel();
            this.refreshBoss();
            this.showTick();
            this.showBossTip(0 == DataCenter.getInstance().totalData.currentLevel % 5 ? "2": "1")
        };
        b.prototype.bossKilled = function() {
            this._fightStatusBtn.setStatus(3);
            this._lvView.showLevel();
            this.refreshBoss();
            this._tickID && Time.clearInterval(this._tickID);
            this._tickTxt.visible = !1;
            this._bossTimeBar.visible = !1
        };
        b.prototype.showTick = function() {
            this._tickID && Time.clearInterval(this._tickID);
            this._tickTime = 30;
            var a = applyFilters(Const.GET_HOLY_EFFECT, getApplyString(25));
            a != getApplyString(25) && (this._tickTime *= 1 + a);
            this._bossTime = this._tickTime;
            this._tickTxt.visible = !0;
            this._bossTimeBar.visible = !0;
            this._bossTimeBar.scaleX = 1;
            this._tickID = Time.setInterval(this.tick, this, 100)
        };
        b.prototype.tick = function() {
            this._tickTime -= 0.1;
            this._tickTxt.text = this._tickTime.toFixed(1);
            this._bossTimeBar.scaleX = this._tickTime / this._bossTime;
            0 >= this._tickTime && (Time.clearInterval(this._tickID), doAction(Const.LEAVE_BATTLE))
        };
        b.prototype.showBossTip = function(a) {
            var b = this;
            GameUtils.preloadAnimation("boss_json",
                function() {
                    null == b._bossMC && (b._bossMC = new Movie("boss_json", "boss"), b._bossMC.autoRemove = !1, b._bossMC.x = 320, b._bossMC.y = 270, b.addChild(b._bossMC));
                    b._bossMC.play(a)
                })
        };
        b.prototype.initTouchArea = function() {
            this._touchArea = new egret.Sprite;
            this._touchArea.width = GameUtils.getWinWidth();
            this._touchArea.height = 640;
            this._touchArea.touchEnabled = !0;
            this.addChild(this._touchArea);
            this._touchArea.addEventListener(egret.TouchEvent.TOUCH_TAP, this.hitHero, this);
            this._touchArea.addEventListener(egret.TouchEvent.TOUCH_BEGIN, this.touchBegin, this);
            this._touchArea.addEventListener(egret.TouchEvent.TOUCH_END, this.touchEnd, this);
            this._touchArea.addEventListener(egret.TouchEvent.TOUCH_RELEASE_OUTSIDE, this.touchEnd, this)
        };
        b.prototype.touchBegin = function() {
            DataCenter.getInstance().hasPaymentCountdown("touch") && (DataCenter.getInstance().statistics.addValue(StatisticsType.tap), this._skillTickNum = 0, this._skillTimeID = Time.setInterval(this.autoHit1, this, 1E3 / 33))
        };
        b.prototype.autoHit1 = function() {
            if (DataCenter.getInstance().hasPaymentCountdown("touch")) {
                this._skillTickNum += 1;
                SoundManager.getInstance().playEffect("swingSound");
                var a = DataCenter.getInstance().getAttackInfo();
                a.crit && (new ShakeAnimation(100, 10, 5)).run(this._bg);
                0 == this._skillTickNum % 3 ? this._boss.attacked(a.dmg) && this.addHurtText(a) : this._boss.attackedWithoutAction(a.dmg)
            } else this.touchEnd()
        };
        b.prototype.touchEnd = function() {
            Time.clearInterval(this._skillTimeID)
        };
        b.prototype.showClickAction = function(a) {
            RES.isGroupLoaded("group_click_json") && (null == this._mv && (this._mv = new Movie("click_json", "click"), this._mv.autoRemove = !1, this.addChild(this._mv)), this._mv.play("1"), this._mv.x = a.stageX, this._mv.y = a.stageY)
        };
        b.prototype.hitHero = function(a) {
            this.showClickAction(a);
            doAction(Guide.CLICK_HOME);
            DataCenter.getInstance().statistics.addValue(StatisticsType.tap);
            SoundManager.getInstance().playEffect("swingSound");
            a = DataCenter.getInstance().getAttackInfo();
            a.crit && (new ShakeAnimation(100, 10, 5)).run(this._bg);
            var b = this._boss.attacked(a.dmg);
            this._mainHero.attack();
            b && this.addHurtText(a)
        };
        b.prototype.addHurtText = function(a) {
            var b = GameUtils.getHurtTxt(a.dmg);
            b.x = 320;
            b.y = 300;
            b.start(a.crit);
            this.addChild(b)
        };
        b.prototype.initSetButton = function() {
            this._setButton = new SetButton;
            this.addChild(this._setButton);
            this._setButton.x = this._setButton.y = 50;
            this._setButton.anchorX = this._setButton.anchorY = 0.5;
            this._achieveButton = new AchieveEntryButton;
            this.addChild(this._achieveButton);
            this._achieveButton.x = 120;
            this._achieveButton.y = 50;
            this._topButton = new SimpleButton;
            this._topButton.anchorX = this._topButton.anchorY = 0.5;
            this._topButton.x = 50;
            this._topButton.y = 113;
            var a = ResourceUtils.createBitmapFromSheet("order", "commonRes");
            this._topButton.addChild(a);
            this._topButton.addOnClick(this.topClickHandler, this);
            this._topButton.useZoomOut(!0);
            this.addChild(this._topButton);
            ProxyUtils.platformInfo == platformType_wanba && (this._topButton.visible = !1);
            PlatformFactory.getPlatform().name == PlatformTypes.WEIXIN && (a = new WeixinButton, a.x = 50, a.y = 170, this.addChild(a))
        };
        b.prototype.topClickHandler = function() {
            GameMainLayer.instance.showTopListLayer()
        };
        b.prototype.initCoinView = function() {
            this._coinView = new CoinView;
            this.addChild(this._coinView);
            this._coinView.x = 230;
            this._coinView.y = 70
        };
        b.prototype.initHero = function() {
            this._actionContainer = new egret.DisplayObjectContainer;
            this.addChild(this._actionContainer);
            this._heroContainer = new egret.DisplayObjectContainer;
            this.addChild(this._heroContainer);
            this._heroContainer.y = 530;
            this._heroContainer.x = 320;
            var a = HeroController.getInstance().mainHero;
            this._mainHero = new Hero(a);
            this._heroContainer.addChild(this._mainHero);
            for (var b = HeroController.getInstance().getAliveHero(), c = 0; c < b.length; c++) {
                var a = b[c],
                    e = new Hero(a);
                this._heroContainer.addChild(e);
                1013 <= a.configId && (e.x = HeroActionPos[a.configId].x - 320, e.y = HeroActionPos[a.configId].y - 530)
            }
        };
        b.prototype.refreshHome = function() {
            this._bgContainer || (this._bgContainer = new egret.DisplayObjectContainer, this.addChild(this._bgContainer));
            this._bgContainer.removeChildren();
            var a = ResourceUtils.createBitmapByName("home_scene_default");
            this._bgContainer.addChild(a);
            a = GameUtils.getMapRes();
            this._bg = egret.DynamicBitmap.create("home_scene_" + a, !1, this.loadComplete, this)
        };
        b.prototype.loadComplete = function() {
            this._bgContainer.removeChildren();
            this._bgContainer.addChild(this._bg)
        };
        b.prototype.initBoss = function(a) {
            void 0 === a && (a = !0);
            null == this._boss && (this._boss = new Boss, this._boss.y = -105, this.addChild(this._boss));
            null == this._lvView && (this._lvView = new BossLevelView, this.addChild(this._lvView), this._lvView.x = 300, this._lvView.y = 70);
            null == this._fightStatusBtn && (this._fightStatusBtn = new BattleStatusButton, this._fightStatusBtn.initView(), this._fightStatusBtn.x = 550, this._fightStatusBtn.y = 60, this.addChild(this._fightStatusBtn));
            a && this._boss.refreshBoss()
        };
        b.prototype.refreshBoss = function() {
            this.initBoss();
            this._lvView.refreshView()
        };
        b.prototype.addHero = function(a) {
            this.showAnimation(a)
        };
        b.prototype.showAnimation = function(a) {
            if (RES.isGroupLoaded("group_hero_upgrade_json")) {
                var b = HeroActionPos[a.configId],
                    c = new Movie("hero_upgrade_json", "hero_upgrade");
                this._actionContainer.addChild(c);
                c.setCallback(function() {
                        var b = new Hero(a);
                        this._heroContainer.addChild(b);
                        1013 <= a.configId && (b.x = HeroActionPos[a.configId].x - 320, b.y = HeroActionPos[a.configId].y - 530)
                    },
                    this);
                c.play("3");
                c.x = b.x;
                c.y = b.y
            } else b = new Hero(a),
                this._heroContainer.addChild(b)
        };
        b.prototype.heroUpgradeHandler = function(a) {
            if (RES.isGroupLoaded("group_hero_upgrade_json")) {
                a = HeroActionPos[a.configId];
                var b = new Movie("hero_upgrade_json", "hero_upgrade");
                this._actionContainer.addChild(b);
                b.play("1");
                b.x = a.x;
                b.y = a.y
            }
        };
        b.prototype.addHero1 = function(a) {
            this.addHero(a)
        };
        return b
    } (BaseContainer);
GameLayer.prototype.__class__ = "GameLayer";
var HeroActionPos = {
        1E3: {
            x: 323,
            y: 520
        },
        1001 : {
            x: 470,
            y: 520
        },
        1002 : {
            x: 175,
            y: 520
        },
        1003 : {
            x: 537,
            y: 520
        },
        1004 : {
            x: 125,
            y: 520
        },
        1005 : {
            x: 530,
            y: 420
        },
        1006 : {
            x: 95,
            y: 420
        },
        1007 : {
            x: 65,
            y: 520
        },
        1008 : {
            x: 605,
            y: 420
        },
        1009 : {
            x: 605,
            y: 520
        },
        1010 : {
            x: 46,
            y: 420
        },
        1011 : {
            x: 50,
            y: 210
        },
        1012 : {
            x: 560,
            y: 210
        },
        1013 : {
            x: 20,
            y: 310
        },
        1014 : {
            x: 585,
            y: 520
        },
        1015 : {
            x: 555,
            y: 520
        },
        1016 : {
            x: 485,
            y: 520
        },
        1017 : {
            x: 202,
            y: 520
        },
        1018 : {
            x: 30,
            y: 520
        },
        1019 : {
            x: 130,
            y: 420
        },
        1020 : {
            x: 570,
            y: 420
        },
        1021 : {
            x: 510,
            y: 420
        },
        1022 : {
            x: 630,
            y: 420
        },
        1023 : {
            x: 620,
            y: 310
        },
        1024 : {
            x: 20,
            y: 420
        },
        1025 : {
            x: 70,
            y: 420
        },
        1026 : {
            x: 575,
            y: 310
        },
        1027 : {
            x: 525,
            y: 310
        },
        1028 : {
            x: 120,
            y: 310
        },
        1029 : {
            x: 80,
            y: 310
        },
        1030 : {
            x: 605,
            y: 200
        }
    },
    testHero = null,
    __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    ShareLayer = function(c) {
        function b() {
            c.call(this);
            this.init()
        }
        __extends(b, c);
        b.prototype.init = function() {
            this._container = new egret.DisplayObjectContainer;
            this.addChild(this._container);
            this._awardContainer = new egret.DisplayObjectContainer;
            this._awardContainer.y = -50;
            var a = ResourceUtils.createBitmapByName("mask");
            this._container.addChild(a);
            a.touchEnabled = !0;
            a = ResourceUtils.createBitmapByName("share_bg");
            this._container.addChild(a);
            a.x = 25;
            a = ResourceUtils.createBitmapFromSheet("share_info_bg");
            a.x = 65;
            a.y = 220;
            this._container.addChild(a);
            a = ResourceUtils.createBitmapFromSheet("share_tip");
            a.x = 55;
            a.y = 228;
            this._container.addChild(a);
            this._msgLabel = new egret.TextField;
            this._container.addChild(this._msgLabel);
            this._msgLabel.text = "CONTENT";
            this._msgLabel.anchorX = 0;
            this._msgLabel.anchorY = 1;
            this._msgLabel.textColor = 16777215;
            this._msgLabel.x = 200;
            this._msgLabel.y = 270;
            this._tipLabel = new egret.TextField;
            this._container.addChild(this._tipLabel);
            this._tipLabel.textAlign = egret.HorizontalAlign.CENTER;
            this._tipLabel.textColor = 15710803;
            this._tipLabel.anchorX = this._tipLabel.anchorY = 0.5;
            this._tipLabel.text = "\u597d\u53cb\u70b9\u51fb\u5206\u4eab\u4fe1\u606f\u5c06\u53ef\u4ee5\u7acb\u5373\u4f7f\u7528\u6280\u80fd";
            this._tipLabel.x = 320;
            this._tipLabel.y = 400;
            a = ResourceUtils.createBitmapFromSheet("share_award_bg");
            a.x = 125;
            a.y = 440;
            a.scaleX = 1.1;
            this._awardContainer.addChild(a);
            this._awardLabel = new egret.TextField;
            this._awardContainer.addChild(this._awardLabel);
            this._awardLabel.x = 250;
            this._awardLabel.y = 485;
            this._awardLabel.textFlow = (new egret.HtmlTextParser).parser('<font color="0xffffff">\u5956\u52b1\uff1a</font><font color="0xEBA111">100\u94bb\u77f3</font>');
            this._container.cacheAsBitmap = !0;
            this._iconContainer = new egret.DisplayObjectContainer;
            this._awardContainer.addChild(this._iconContainer);
            this._iconContainer.x = 190;
            this._iconContainer.y = 495;
            this._buttons = new egret.DisplayObjectContainer;
            this.addChild(this._buttons);
            this._container.addChild(this._awardContainer)
        };
        b.prototype.createButton = function(a, b, c, e, h, f, k, l, m) {
            var p = new egret.DisplayObjectContainer,
                n = new SimpleButton;
            n.width = f;
            n.height = k;
            n.x = e;
            n.y = h;
            n.useZoomOut(!0);
            n.addOnClick(c, this);
            n.anchorX = n.anchorY = 0.5;
            n.addChild(p);
            p.cacheAsBitmap = !0;
            b = ResourceUtils.createBitmapFromSheet(b);
            b.anchorX = b.anchorY = 0.5;
            p.addChild(b);
            b.x = f / 2;
            b.y = k / 2;
            b = new egret.TextField;
            b.anchorX = b.anchorY = 0.5;
            p.addChild(b);
            b.text = a;
            b.x = f / 2 + l;
            b.y = k / 2 + m;
            b.size = 25;
            this._buttons.addChild(n)
        };
        b.prototype.refreshView = function(a, b, c) {
            void 0 === b && (b = null);
            void 0 === c && (c = null);
            this._selector = b;
            this._context = c;
            this._conf = a;
            this._msgLabel.text = a.title;
            this._tipLabel.text = a.tip || "";
            GameUtils.removeAllChildren(this._buttons);
            this.showGold(a);
            this.showDiamond(a);
            a.ad ? this.createAdButttons() : "diamond" == a.type ? this.createDiamondButtons() : "gold" == a.type && this.createGoldButtons()
        };
        b.prototype.showGold = function(a) {
            if ("gold" == a.type) {
                var b = new NumberModel;
                b.add(a.award);
                this._awardLabel.textFlow = (new egret.HtmlTextParser).parser('<font color="0xffffff">\u5956\u52b1:</font><font color="0xEBA111">' + b.getNumer() + "\u91d1\u5e01</font>");
                GameUtils.removeAllChildren(this._iconContainer);
                a = ResourceUtils.createBitmapFromSheet("gold_border");
                a.anchorX = a.anchorY = 0.5;
                this._iconContainer.addChild(a);
                a = ResourceUtils.createBitmapFromSheet("icon_payment_1", "iconRes");
                a.anchorX = a.anchorY = 0.5;
                this._iconContainer.addChild(a)
            }
        };
        b.prototype.createGoldButtons = function() {
            this.createButton("\u9886\u53d6\u5956\u52b1", "share_btn", this.close, 315, 633, 180, 70, 0, -3)
        };
        b.prototype.createAdButttons = function() {
            this.createButton("\u4e0d\u4e86\uff0c\u8c22\u8c22", "share_btn", this.close.bind(this, !1), 200, 633, 180, 70, 0, -3);
            this.createButton("\u89c2\u770b\u5e7f\u544a", "long_btn", this.showAdLayer, 440, 633, 180, 70, 0, -3)
        };
        b.prototype.showAdLayer = function() {
            this.close(!1);
            GameMainLayer.instance.showAdLayer(this._conf)
        };
        b.prototype.showDiamond = function(a) {
            "diamond" == a.type && (this._awardLabel.textFlow = (new egret.HtmlTextParser).parser('<font color="0xffffff">\u5956\u52b1:</font><font color="0xEBA111">' + a.award + "\u94bb\u77f3</font>"), GameUtils.removeAllChildren(this._iconContainer), a = ResourceUtils.createBitmapFromSheet("gold_border"), a.anchorX = a.anchorY = 0.5, this._iconContainer.addChild(a), a = ResourceUtils.createBitmapFromSheet("icon_payment_8", "iconRes"), a.anchorX = a.anchorY = 0.5, this._iconContainer.addChild(a))
        };
        b.prototype.createDiamondButtons = function() {
            this.createButton("\u9886\u53d6\u5956\u52b1", "share_btn", this.close, 315, 633, 180, 70, 0, -3)
        };
        b.prototype.close = function(a) {
            void 0 === a && (a = !0);
            GameMainLayer.instance.hideShareLayer();
            this._selector && this._selector.call(this._context, "close");
            a && doAction(Const.AWARD_EFFECT, this._conf)
        };
        b.prototype.share = function() {
            this._selector && this._selector.call(this._context)
        };
        return b
    } (BaseContainer);
ShareLayer.prototype.__class__ = "ShareLayer";
var __extends = this.__extends ||
        function(c, b) {
            function a() {
                this.constructor = c
            }
            for (var d in b) b.hasOwnProperty(d) && (c[d] = b[d]);
            a.prototype = b.prototype;
            c.prototype = new a
        },
    ShareTipLayer = function(c) {
        function b() {
            c.call(this);
            this.init()
        }
        __extends(b, c);
        b.prototype.init = function() {
            this._container = new egret.DisplayObjectContainer;
            this.addChild(this._container);
            var a = ResourceUtils.createBitmapByName("mask");
            this._container.addChild(a);
            a.touchEnabled = !0;
            this._container.cacheAsBitmap = !0;
            this._bgContainer = new egret.DisplayObjectContainer;
            this._container.addChild(this._bgContainer);
            var a = new egret.DisplayObjectContainer,
                b = new SimpleButton;
            b.useZoomOut(!0);
            b.addOnClick(this.close, this);
            b.anchorX = b.anchorY = 0.5;
            b.width = 90;
            b.height = 87;
            b.x = 450;
            b.y = 170;
            b.addChild(a);
            a.cacheAsBitmap = !0;
            var c = ResourceUtils.createBitmapFromSheet("share_tip_btn");
            c.anchorX = c.anchorY = 0.5;
            a.addChild(c);
            c.x = 45;
            c.y = 43;
            this.addChild(b)
        };
        b.prototype.close = function() {
            GameMainLayer.instance.hideShareTipLayer();
            doAction(Const.CLOSE_SHARE_TIP)
        };
        b.prototype.show = function(a) {
            "awardbox" == a ? (GameUtils.removeAllChildren(this._bgContainer), a = ResourceUtils.createBitmapFromSheet("share_0", "shareRes"), this._bgContainer.addChild(a), a = new egret.TextField, a.textFlow = (new egret.HtmlTextParser).parser('\u201c\u6211\u73a9\u6e38\u620f\u83b7\u5f97\u4e86<font color="0xff0000">' + ProxyUtils.awdTo.awddes + "</font>\uff0c\n\u8fd8\u6709iPhone\u3001iPad\uff0c\u662f\u670b\u53cb\u624d\u544a\u8bc9\u4f60\uff0c\n\u8fd8\u4e0d\u8d76\u7d27\u6765\uff01\u201d"), this._bgContainer.addChild(a), a.y = 250, a.size = 35, a.x = 14, a.lineSpacing = 10, a.textAlign = egret.HorizontalAlign.CENTER, a.bold = !0, a.textColor = 0) : (GameUtils.removeAllChildren(this._bgContainer), a = ResourceUtils.createBitmapFromSheet(a, "shareRes"), this._bgContainer.addChild(a))
        };
        return b
    } (BaseContainer);
ShareTipLayer.prototype.__class__ = "ShareTipLayer";
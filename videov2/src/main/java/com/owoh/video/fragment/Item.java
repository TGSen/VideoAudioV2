package com.owoh.video.fragment;

import java.util.List;

public class Item {

    /**
     * date : 20180507
     * message : Success !
     * status : 200
     * city : 广州
     * count : 1642
     * data : {"shidu":"86%","pm25":14,"pm10":24,"quality":"优","wendu":"26","ganmao":"各类人群可自由活动","yesterday":{"date":"06日星期日","sunrise":"05:52","high":"高温 30.0℃","low":"低温 24.0℃","sunset":"18:56","aqi":44,"fx":"无持续风向","fl":"<3级","type":"雷阵雨","notice":"带好雨具，别在树下躲雨"},"forecast":[{"date":"07日星期一","sunrise":"05:51","high":"高温 27.0℃","low":"低温 21.0℃","sunset":"18:57","aqi":43,"fx":"无持续风向","fl":"<3级","type":"大到暴雨","notice":"雨势转大，在外找好避雨处"},{"date":"08日星期二","sunrise":"05:51","high":"高温 26.0℃","low":"低温 21.0℃","sunset":"18:57","aqi":49,"fx":"无持续风向","fl":"<3级","type":"大到暴雨","notice":"雨势转大，在外找好避雨处"},{"date":"09日星期三","sunrise":"05:50","high":"高温 25.0℃","low":"低温 21.0℃","sunset":"18:58","aqi":57,"fx":"无持续风向","fl":"<3级","type":"中雨","notice":"记得随身携带雨伞哦"},{"date":"10日星期四","sunrise":"05:49","high":"高温 26.0℃","low":"低温 21.0℃","sunset":"18:58","aqi":53,"fx":"东南风","fl":"3-4级","type":"多云","notice":"阴晴之间，谨防紫外线侵扰"},{"date":"11日星期五","sunrise":"05:49","high":"高温 28.0℃","low":"低温 22.0℃","sunset":"18:59","aqi":45,"fx":"东风","fl":"3-4级","type":"阵雨","notice":"阵雨来袭，出门记得带伞"}]}
     */

    private String date;
    private String message;
    private int status;
    private String city;
    private int count;
    private DataBean data;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * shidu : 86%
         * pm25 : 14
         * pm10 : 24
         * quality : 优
         * wendu : 26
         * ganmao : 各类人群可自由活动
         * yesterday : {"date":"06日星期日","sunrise":"05:52","high":"高温 30.0℃","low":"低温 24.0℃","sunset":"18:56","aqi":44,"fx":"无持续风向","fl":"<3级","type":"雷阵雨","notice":"带好雨具，别在树下躲雨"}
         * forecast : [{"date":"07日星期一","sunrise":"05:51","high":"高温 27.0℃","low":"低温 21.0℃","sunset":"18:57","aqi":43,"fx":"无持续风向","fl":"<3级","type":"大到暴雨","notice":"雨势转大，在外找好避雨处"},{"date":"08日星期二","sunrise":"05:51","high":"高温 26.0℃","low":"低温 21.0℃","sunset":"18:57","aqi":49,"fx":"无持续风向","fl":"<3级","type":"大到暴雨","notice":"雨势转大，在外找好避雨处"},{"date":"09日星期三","sunrise":"05:50","high":"高温 25.0℃","low":"低温 21.0℃","sunset":"18:58","aqi":57,"fx":"无持续风向","fl":"<3级","type":"中雨","notice":"记得随身携带雨伞哦"},{"date":"10日星期四","sunrise":"05:49","high":"高温 26.0℃","low":"低温 21.0℃","sunset":"18:58","aqi":53,"fx":"东南风","fl":"3-4级","type":"多云","notice":"阴晴之间，谨防紫外线侵扰"},{"date":"11日星期五","sunrise":"05:49","high":"高温 28.0℃","low":"低温 22.0℃","sunset":"18:59","aqi":45,"fx":"东风","fl":"3-4级","type":"阵雨","notice":"阵雨来袭，出门记得带伞"}]
         */

        private String shidu;
        private int pm25;
        private int pm10;
        private String quality;
        private String wendu;
        private String ganmao;
        private YesterdayBean yesterday;
        private List<ForecastBean> forecast;

        public String getShidu() {
            return shidu;
        }

        public void setShidu(String shidu) {
            this.shidu = shidu;
        }

        public int getPm25() {
            return pm25;
        }

        public void setPm25(int pm25) {
            this.pm25 = pm25;
        }

        public int getPm10() {
            return pm10;
        }

        public void setPm10(int pm10) {
            this.pm10 = pm10;
        }

        public String getQuality() {
            return quality;
        }

        public void setQuality(String quality) {
            this.quality = quality;
        }

        public String getWendu() {
            return wendu;
        }

        public void setWendu(String wendu) {
            this.wendu = wendu;
        }

        public String getGanmao() {
            return ganmao;
        }

        public void setGanmao(String ganmao) {
            this.ganmao = ganmao;
        }

        public YesterdayBean getYesterday() {
            return yesterday;
        }

        public void setYesterday(YesterdayBean yesterday) {
            this.yesterday = yesterday;
        }

        public List<ForecastBean> getForecast() {
            return forecast;
        }

        public void setForecast(List<ForecastBean> forecast) {
            this.forecast = forecast;
        }

        public static class YesterdayBean {
            /**
             * date : 06日星期日
             * sunrise : 05:52
             * high : 高温 30.0℃
             * low : 低温 24.0℃
             * sunset : 18:56
             * aqi : 44
             * fx : 无持续风向
             * fl : <3级
             * type : 雷阵雨
             * notice : 带好雨具，别在树下躲雨
             */

            private String date;
            private String sunrise;
            private String high;
            private String low;
            private String sunset;
            private int aqi;
            private String fx;
            private String fl;
            private String type;
            private String notice;

            public String getDate() {
                return date;
            }

            public void setDate(String date) {
                this.date = date;
            }

            public String getSunrise() {
                return sunrise;
            }

            public void setSunrise(String sunrise) {
                this.sunrise = sunrise;
            }

            public String getHigh() {
                return high;
            }

            public void setHigh(String high) {
                this.high = high;
            }

            public String getLow() {
                return low;
            }

            public void setLow(String low) {
                this.low = low;
            }

            public String getSunset() {
                return sunset;
            }

            public void setSunset(String sunset) {
                this.sunset = sunset;
            }

            public int getAqi() {
                return aqi;
            }

            public void setAqi(int aqi) {
                this.aqi = aqi;
            }

            public String getFx() {
                return fx;
            }

            public void setFx(String fx) {
                this.fx = fx;
            }

            public String getFl() {
                return fl;
            }

            public void setFl(String fl) {
                this.fl = fl;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getNotice() {
                return notice;
            }

            public void setNotice(String notice) {
                this.notice = notice;
            }
        }

        public static class ForecastBean {
            /**
             * date : 07日星期一
             * sunrise : 05:51
             * high : 高温 27.0℃
             * low : 低温 21.0℃
             * sunset : 18:57
             * aqi : 43
             * fx : 无持续风向
             * fl : <3级
             * type : 大到暴雨
             * notice : 雨势转大，在外找好避雨处
             */

            private String date;
            private String sunrise;
            private String high;
            private String low;
            private String sunset;
            private int aqi;
            private String fx;
            private String fl;
            private String type;
            private String notice;

            public String getDate() {
                return date;
            }

            public void setDate(String date) {
                this.date = date;
            }

            public String getSunrise() {
                return sunrise;
            }

            public void setSunrise(String sunrise) {
                this.sunrise = sunrise;
            }

            public String getHigh() {
                return high;
            }

            public void setHigh(String high) {
                this.high = high;
            }

            public String getLow() {
                return low;
            }

            public void setLow(String low) {
                this.low = low;
            }

            public String getSunset() {
                return sunset;
            }

            public void setSunset(String sunset) {
                this.sunset = sunset;
            }

            public int getAqi() {
                return aqi;
            }

            public void setAqi(int aqi) {
                this.aqi = aqi;
            }

            public String getFx() {
                return fx;
            }

            public void setFx(String fx) {
                this.fx = fx;
            }

            public String getFl() {
                return fl;
            }

            public void setFl(String fl) {
                this.fl = fl;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getNotice() {
                return notice;
            }

            public void setNotice(String notice) {
                this.notice = notice;
            }
        }
    }
}

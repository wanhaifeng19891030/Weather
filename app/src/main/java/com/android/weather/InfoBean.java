package com.android.weather;

import java.io.Serializable;
import java.util.List;

public class InfoBean implements Serializable {

    private String temp;
    private String city;
    private String cityid;
    private String citycode;
    private String date;
    private String week;
    private String weather;
    private String temphigh;
    private String templow;
    private List<Index> index;
    private List<Daily> daily;
    private Aqi aqi;
    private String humidity;
    private String  pressure;
    private String  windspeed;
    private String winddirect;
    private List<Hourly> hourly;
    private String updatetime;
    private String windpower;
    private String img;
    private String currenttime;

    public String getCurrenttime(){
        return currenttime;
    }
    public void setCurrenttime(String currenttime){
        this.currenttime = currenttime;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getWindpower() {
        return windpower;
    }

    public void setWindpower(String windpower) {
        this.windpower = windpower;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public List<Hourly> getHourly() {
        return hourly;
    }

    public void setHourly(List<Hourly> hourly) {
        this.hourly = hourly;
    }

    public class Hourly{
       private String time;
       private String weather;
       private String temp;
       private String img;

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getWeather() {
            return weather;
        }

        public void setWeather(String weather) {
            this.weather = weather;
        }

        public String getTemp() {
            return temp;
        }

        public void setTemp(String temp) {
            this.temp = temp;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }
    }

    public List<Daily> getDaily() {
        return daily;
    }

    public void setDaily(List<Daily> daily) {
        this.daily = daily;
    }

    public class Daily{
        private String sunrise;
        private String sunset;
        private String date;
        private String week;
        private Night night;
        private Day day;

        public Night getNight() {
            return night;
        }

        public void setNight(Night night) {
            this.night = night;
        }

        public Day getDay() {
            return day;
        }

        public void setDay(Day day) {
            this.day = day;
        }

        public class Day{
            private String weather;
            private String temphigh;
            private String img;
            private String winddirect;
            private String windpower;

            public String getWindpower() {
                return windpower;
            }

            public void setWindpower(String windpower) {
                this.windpower = windpower;
            }

            public String getWeather() {
                return weather;
            }

            public void setWeather(String weather) {
                this.weather = weather;
            }

            public String getTemphigh() {
                return temphigh;
            }

            public void setTemphigh(String temphigh) {
                this.temphigh = temphigh;
            }

            public String getImg() {
                return img;
            }

            public void setImg(String img) {
                this.img = img;
            }

            public String getWinddirect() {
                return winddirect;
            }

            public void setWinddirect(String winddirect) {
                this.winddirect = winddirect;
            }
        }

        public class Night{
            private String weather;
            private String templow;
            private String img;
            private String winddirect;
            private String windpower;

            public String getWindpower() {
                return windpower;
            }

            public void setWindpower(String windpower) {
                this.windpower = windpower;
            }

            public String getWeather() {
                return weather;
            }

            public void setWeather(String weather) {
                this.weather = weather;
            }

            public String getTemplow() {
                return templow;
            }

            public void setTemplow(String templow) {
                this.templow = templow;
            }

            public String getImg() {
                return img;
            }

            public void setImg(String img) {
                this.img = img;
            }

            public String getWinddirect() {
                return winddirect;
            }

            public void setWinddirect(String winddirect) {
                this.winddirect = winddirect;
            }
        }

        public String getSunrise() {
            return sunrise;
        }

        public void setSunrise(String sunrise) {
            this.sunrise = sunrise;
        }

        public String getSunset() {
            return sunset;
        }

        public void setSunset(String sunset) {
            this.sunset = sunset;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getWeek() {
            return week;
        }

        public void setWeek(String week) {
            this.week = week;
        }
    }

    public String getWinddirect() {
        return winddirect;
    }

    public void setWinddirect(String winddirect) {
        this.winddirect = winddirect;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getWindspeed() {
        return windspeed;
    }

    public void setWindspeed(String windspeed) {
        this.windspeed = windspeed;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getTemphigh() {
        return temphigh;
    }

    public void setTemphigh(String temphigh) {
        this.temphigh = temphigh;
    }

    public String getTemplow() {
        return templow;
    }

    public void setTemplow(String templow) {
        this.templow = templow;
    }

    public Aqi getAqi() {
        return aqi;
    }

    public void setAqi(Aqi aqi) {
        this.aqi = aqi;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public List<Index> getIndex() {
        return index;
    }

    public void setIndex(List<Index> index){
       this.index = index;
    }

    public String getCity() {
        return city;
    }

    public class Index{

        private String iname;
        private String ivalue;
        private String detail;

        public void setIname(String iname){
           this.iname = iname;
        }
        public String getIname(){
            return iname;
        }

        public void setIvalue(String ivalue){
            this.ivalue = ivalue;
        }
        public String getIvalue(){
            return ivalue;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }
    }

    public class Aqi{

        public String getQuality() {
            return quality;
        }

        public void setQuality(String quality) {
            this.quality = quality;
        }

        public String getIpm2_5() {
            return ipm2_5;
        }

        public void setIpm2_5(String ipm2_5) {
            this.ipm2_5 = ipm2_5;
        }

        public String getIpm10() {
            return ipm10;
        }

        public void setIpm10(String ipm10) {
            this.ipm10 = ipm10;
        }

        public String getIso2() {
            return iso2;
        }

        public void setIso2(String iso2) {
            this.iso2 = iso2;
        }

        public String getIno2() {
            return ino2;
        }

        public void setIno2(String ino2) {
            this.ino2 = ino2;
        }

        public String getAqi() {
            return aqi;
        }

        public void setAqi(String aqi) {
            this.aqi = aqi;
        }

        private String quality;
        private String ipm2_5;
        private String ipm10;
        private String iso2;
        private String ino2;
        private String aqi;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityid() {
        return cityid;
    }

    public void setCityid(String cityid) {
        this.cityid = cityid;
    }

    public String getCitycode() {
        return citycode;
    }

    public void setCitycode(String citycode) {
        this.citycode = citycode;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }
}

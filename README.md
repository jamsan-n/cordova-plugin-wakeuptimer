# Wakeup/Alarm Clock PhoneGap/Cordova Plugin

### Platform Support

This plugin supports PhoneGap/Cordova apps running on both iOS and Android.

### Version Requirements

This plugin is meant to work with Cordova 3.5.0+.

## Installation

#### Automatic Installation using PhoneGap/Cordova CLI (iOS and Android)
1. Make sure you update your projects to Cordova iOS version 3.5.0+ before installing this plugin.

        cordova platform update ios
        cordova platform update android

2. Install this plugin using PhoneGap/Cordova cli:

        cordova plugin add https://github.com/jamsan-n/cordova-plugin-wakeuptimer.git

## Usage

    // all responses from the audio player are channeled through successCallback and errorCallback

    // set wakeup timer
    window.wakeuptimer.wakeup( successCallback,
       errorCallback,
       // a list of alarms to set
       {
            alarms : [{
                type : 'onetime',
                time : { hour : 14, minute : 30 },
                extra : { message : 'json containing app-specific information to be posted when alarm triggers' },
                message : 'Alarm has expired!'
				skipOnAwake: false,
				skipOnRunning: false,
				startInBackground: true,
           }]
       }
    );

    // set repeating wakeup timer
    window.wakeuptimer.wakeup( successCallback,
       errorCallback,
       // a list of alarms to set
       {
            alarms : [{
                type : 'repeating',
                time : { minutes : 10 },
                extra : { message : 'json containing app-specific information to be posted when alarm triggers' },
                message : 'Alarm has expired!'
				skipOnAwake: false,
				skipOnRunning: false,
				startInBackground: true,
           }]
       }
    );

    // set timer, but skip launch if user is using the phone (screen is on)
    window.wakeuptimer.wakeup( successCallback,
       errorCallback,
       // a list of alarms to set
       {
            alarms : [{
                type : 'onetime',
                skipOnAwake: true,
                time : { hour : 12, minute : 10 },
                extra : { message : 'json containing app-specific information to be posted when alarm triggers' },
                message : 'Alarm has expired!'
				skipOnAwake: false,
				skipOnRunning: false,
				startInBackground: true,
           }]
       }
    );

    // set wakeup timer, but skip launch if app is already running
    window.wakeuptimer.wakeup( successCallback,
       errorCallback,
       // a list of alarms to set
       {
            alarms : [{
                type : 'onetime',
                time : { hour : 11, minute : 20 },
                extra : { message : 'json containing app-specific information to be posted when alarm triggers' },
                message : 'Alarm has expired!'
				skipOnAwake: false,
				startInBackground: true,
                skipOnRunning: true,
           }]
       }
    );

    // snooze...
    window.wakeuptimer.snooze( successCallback,
        errorCallback,
        {
            alarms : [{
                type : 'snooze',
                time : { seconds : 60 }, // snooze for 60 seconds
                extra : { }, // json containing app-specific information to be posted when alarm triggers
                message : this.get('message'),
                sound : this.get('sound'),
                action : this.get('action')
				skipOnAwake: false,
				skipOnRunning: false,
				startInBackground: true,
            }]
        }
     );

    // set repeating by seconds
    window.wakeuptimer.wakeup( successCallback,
       errorCallback,
       // a list of alarms to set
       {
            alarms : [{
                type : 'repeatingSeconds',
                time : {seconds: 20},
                extra : { message : 'json containing app-specific information to be posted when alarm triggers' },
                message : 'Alarm has expired!'
				skipOnAwake: false,
				skipOnRunning: false,
				startInBackground: true,
           }]
       }
    );


    // example of a callback method
    var successCallback = function(result) {
        if (result.type==='wakeup') {
            console.log('wakeup alarm detected--' + result.extra);
        } else if(result.type==='set'){
            console.log('wakeup alarm set--' + result);
        } else {
            console.log('wakeup unhandled type (' + result.type + ')');
        }
    };

	// ex repeatingSeconds callback:
	```
	wakeup repeating res: {type: "set", alarm_type: "repeatingSeconds", alarm_date: 1566908703500}
	wakeup repeating res: {type: "wakeup", extra: "{ message : 'json containing app-specific information to be posted when alarm triggers' }", cdvStartInBackground: true}
	```

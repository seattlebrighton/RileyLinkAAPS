# Roundtrip2 Medtronic AAPS

This is fork from original Roundtrip2 branch, which was intended as Pump Driver for HAPP application. 

If you need driver for HAPP please go to [this repository](https://github.com/TC2013/Roundtrip2)

I did little reafactoring of the GUI so that "our" options are more visible. On start page there is "Show AAPS" button, that will switch you to our test screen, where we have buttons for all commands we intend to implement. Commands supported will be colored green, the one in work yellow, and others are normal. History will probably be tested from main screen only.

We are starting from this app, because communication with RileyLink here already works, and that is what is needed for communciation with Medtronic pump (and later also with Omnipod). After we have commands working, we will start with refactoring of RileyLink code, so that it can be used by more than one consumer/pump.








[![Gitter](https://badges.gitter.im/TC2013/Roundtrip2.svg)](https://gitter.im/TC2013/Roundtrip2?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

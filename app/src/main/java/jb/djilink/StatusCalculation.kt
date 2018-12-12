package jb.djilink

/**
 * Created by jan on 12.12.17.
 */

//Berechnet Bitmap ZUm Status

var BASEMODE = 0
var CUSTOMMODE = 0


//Basemode
var isArmed = 0
var isManualInput = 0
var isHIL = 0
var isStabilized = 0
var isGuided = 0
var isAuto = 0
var isTest = 0
var isCustom = 0



fun isArmed(bool: Boolean){
    if (bool){
        isArmed = 128
    } else {
        isArmed = 0
    }
    recalculate()
}

fun isManualInput(bool: Boolean){
    if (bool){
        isManualInput = 64
    } else {
        isManualInput = 0
    }
    recalculate()
}

fun isHIL(bool: Boolean){
    if (bool){
        isHIL = 32
    } else {
        isHIL = 0
    }
    recalculate()
}

fun isStabilized(bool: Boolean){
    if (bool){
        isStabilized = 16
    } else {
        isStabilized = 0
    }
    recalculate()
}

fun isGuided(bool: Boolean){
    if (bool){
        isGuided = 8
    } else {
        isGuided = 0
    }
    recalculate()
}

fun isAuto(bool: Boolean){
    if (bool){
        isAuto = 4
    } else {
        isAuto = 0
    }
    recalculate()
}

fun isTest(bool: Boolean){
    if (bool){
        isTest = 2
    } else {
        isTest = 0
    }
    recalculate()
}

private fun isCustom(bool: Boolean){
    if (bool){
        isCustom = 1
    } else {
        isCustom = 0
    }
}

fun setCustom(custommode: Long){
    CUSTOMMODE = custommode.toInt()
}

fun recalculate(){
    if (CUSTOMMODE != 0) {isCustom(true)} else isCustom(false)
    BASEMODE = isArmed + isManualInput + isHIL + isStabilized + isGuided + isAuto + isTest + isCustom
}



fun isOnCustomWaypointmissionPX4(bool: Boolean){           //nur für Kompatibilität zu QGroundControl benötigt
    if (bool) {
        CUSTOMMODE = 67371008
    } else {
        CUSTOMMODE = 0
    }
    recalculate()
}

fun setModes(basemode: Short, custommode: Long):Boolean{
    var rest = basemode.toInt()
    if (rest - 128 >= 0){
        rest = rest - 128
        if (isArmed != 128) {
            isArmed(true)
            //Todo: Arm
        }
    } else {
        if (isArmed != 0) {
            isArmed(false)
            //Todo: Disarm
        }
    }

    if (rest - 64 >= 0){
        rest = rest - 64
        if (isManualInput != 64) {
            isManualInput(true)
            //Todo: Enable Manual Input
        }
    } else {
        if (isManualInput != 0) {
            isManualInput(false)
            //Todo: Disable Manual Input
        }
    }

    if (rest - 32 >= 0){
        rest = rest - 32
        if (isHIL != 32) {
            isHIL(true)
            //Todo: Enable HIL
        }
    } else {
        if (isHIL != 0) {
            isHIL(false)
            //Todo: Disable HIL
        }
    }

    if (rest - 16 >= 0){
        rest = rest - 16
        if (isStabilized != 16) {
            isStabilized(true)
            //Todo: Stabilize
        }
    } else {
        if (isStabilized != 0) {
            isStabilized(false)
            //Todo: Disable Stabilization
        }
    }

    if (rest - 8 >= 0){
        rest = rest - 8
        if (isGuided != 8) {
            isGuided(true)
            //Todo: Guide
        }
    } else {
        if (isGuided != 0) {
            isGuided(false)
            //Todo: Disable Guide
        }
    }

    if (rest - 4 >= 0){
        rest = rest - 4
        if (isAuto != 4) {
            isAuto(true)
            //Todo: Auto
        }
    } else {
        if (isAuto != 0) {
            isAuto(false)
            //Todo: Disable Auto
        }
    }

    if (rest - 2 >= 0){
        rest = rest - 2
        if (isTest != 2) {
            isTest(true)
            //Todo: Test
        }
    } else {
        if (isTest != 0) {
            isTest(false)
            //Todo: Disable Test
        }
    }

    if (rest - 1 >= 0){
        rest = rest - 1
        if (isCustom != 1) {
            isCustom = 1
            setCustom(custommode)
            //Todo: WaypointMission
        }
    } else {
        if (isCustom != 0) {
            isCustom = 0
            setCustom(custommode)
            //Todo: Stop Waypointmission
        }
    }

    recalculate()

    if (rest == 0){
        return true
    }
    return false

}

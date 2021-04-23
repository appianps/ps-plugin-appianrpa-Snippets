
from flask import Flask, request, jsonify
from ctypes import *

app = Flask(__name__)

class HllApi(object):
    """A class that implements various HLLAPI DLL functions"""

    def __init__(self, dllLoc):
        self.hllDll = WinDLL(dllLoc)
        self.hllapi = self.hllDll.WinHLLAPI

    def connect_presentation_space(self, presentation_space):
        function_number = c_int(1)
        data_string = c_char_p(presentation_space)
        length = c_int(4)
        ps_position = c_int(0)
        self.hllapi(byref(function_number),
                    data_string,
                    byref(length),
                    byref(ps_position))
        return {'returnCode': ps_position.value}

    def disconnect_presentation_space(self):
        function_number = c_int(2)
        data_string = c_char_p()
        length = c_int(4)
        ps_position = c_int(0)
        self.hllapi(byref(function_number),
                    data_string,
                    byref(length),
                    byref(ps_position))
        return {'returnCode': ps_position.value}

    # Query the cursor location.
    def get_cursor(self):
        function_number = c_int(7)
        data_string = c_char_p(b"")
        length = c_int(0)
        ps_position = c_int(0)
        self.hllapi(byref(function_number),
                    data_string,
                    byref(length),
                    byref(ps_position))
        return {'returnCode': ps_position.value, 'position': length.value}

    # Sets the cursor
    def set_cursor(self, position):
        function_number = c_int(40)
        data_string = c_char_p(b"")
        length = c_int(0)
        ps_position = c_int(position)
        self.hllapi(byref(function_number),
                    data_string,
                    byref(length),
                    byref(ps_position))
        return {'returnCode': ps_position.value}

    def send_key(self, key):
        function_number = c_int(3)
        data_string = c_char_p(key)
        length = c_int(len(key))
        ps_position = c_int(0)
        self.hllapi(byref(function_number),
                    data_string,
                    byref(length),
                    byref(ps_position))
        return {'returnCode': ps_position.value}

    def wait(self):
        function_number = c_int(4)
        data_string = c_char_p()
        length = c_int()
        ps_position = c_int()
        self.hllapi(byref(function_number),
                    data_string,
                    byref(length),
                    byref(ps_position))
        return {'returnCode': ps_position.value}

    def copy_presentation_space(self, screen):
        function_number = c_int(5)
        data_string = screen
        length = c_int(8000)
        ps_position = c_int(0)
        self.hllapi(byref(function_number),
                    data_string,
                    byref(length),
                    byref(ps_position))
        return {'returnCode': ps_position.value, 'screen': data_string.decode('latin-1')}

    def search_presentation_space(self, targetString):
        function_number = c_int(6)
        encString = targetString.encode('ascii', 'ignore')
        data_string = c_char_p(encString)
        length = c_int(len(encString))
        ps_position = c_int(0)
        self.hllapi(byref(function_number),
                    data_string,
                    byref(length),
                    byref(ps_position))
        return {'returnCode': ps_position.value, 'position': length.value}

    def copy_presentation_space_to_string(self, targetString):
        function_number = c_int(8)
        targetString = " " * 1920
        data_string = c_char_p(targetString)
        length = c_int(1920)
        ps_position = c_int(0)
        self.hllapi(byref(function_number),
                    data_string, byref(length),
                    byref(ps_position))
        return {'returnCode': ps_position.value, 'screen': data_string.value, }

    def set_session_parameters(self, dataString):
        function_number = c_int(9)
        data_string = dataString
        length = c_int(len(dataString))
        ps_position = c_int(0)
        self.hllapi(byref(function_number),
                    data_string,
                    byref(length),
                    byref(ps_position))
        return {'returnCode': ps_position.value}

    def copyStringToPresentationSpace(self, string, position):
        function_number = c_int(15)
        encString = string.encode('ascii', 'ignore')
        data_string = encString
        length = c_int(len(string))
        ps_position = c_int(position)
        self.hllapi(byref(function_number),
                    data_string,
                    byref(length),
                    byref(ps_position))
        return {'returnCode': ps_position.value}

    def pause(self, time):
        function_number = c_int(18)
        data_string = None
        length = c_int(time)
        ps_position = c_int(0)
        self.hllapi(byref(function_number),
                    data_string,
                    byref(length),
                    byref(ps_position))
        return {'returnCode': ps_position.value}

    def querySessionStatus(self, presentation_space):
        function_number = c_int(22)
        data_string = c_char_p(presentation_space)
        length = c_int(20)
        ps_position = c_int(0)
        self.hllapi(byref(function_number),
                    data_string,
                    byref(length),
                    byref(ps_position))
        return {'returnCode': ps_position.value}

    def startHostNotification(self, params):
        function_number = c_int(23)
        data_string = c_char_p(params)
        length = c_int(16)
        ps_position = c_int(0)
        self.hllapi(byref(function_number),
                    data_string,
                    byref(length),
                    byref(ps_position))
        return {'returnCode': ps_position.value}

    def queryHostUpdate(self, presentation_space):
        function_number = c_int(24)
        data_string = c_char_p(presentation_space)
        length = c_int(4)
        ps_position = c_int(0)
        self.hllapi(byref(function_number),
                    data_string,
                    byref(length),
                    byref(ps_position))
        return {'returnCode': ps_position.value}

    def stopHostNotification(self, presentation_space):
        function_number = c_int(121)
        data_string = c_char_p(presentation_space)
        length = c_int(4)
        ps_position = c_int(0)
        self.hllapi(byref(function_number),
                    data_string, byref(length),
                    byref(ps_position))
        return {'returnCode': ps_position.value}

    def findFieldPosition(self, string, position):
        function_number = c_int(31)
        data_string = c_char_p(string)
        length = c_int(len(string))
        ps_position = c_int(position)
        self.hllapi(byref(function_number),
                    data_string,
                    byref(length),
                    byref(ps_position))
        return {'returnCode': ps_position.value, 'length': length.value, }

    def copyStringToField(self, string, position):
        function_number = c_int(33)
        encString = string.encode('ascii', 'ignore')
        data_string = encString
        length = c_int(len(string))
        ps_position = c_int(position)
        self.hllapi(byref(function_number),
                    data_string,
                    byref(length),
                    byref(ps_position))
        return {'returnCode': ps_position.value}


hllApi = None

@app.route('/init', methods=['POST'])
def init():
    data = request.json
    global hllApi
    hllApi = WinDLL(str(data['dllPath']))

    return "Succesfully initialized API"

@app.route('/connect', methods=['POST'])
def connect():
    data = request.json
    return hllApi.connect_presentation_space(data['presentationSpace'])

@app.route('/sendKey', methods=['POST'])
def send_key():
    data = request.json
    return hllApi.send_key(str(data['text']).encode("utf-8"))

if __name__ == '__main__':
    app.run(debug=True)

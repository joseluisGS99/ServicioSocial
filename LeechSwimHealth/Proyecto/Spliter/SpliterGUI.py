import os
import cv2
from tkinter import *
from tkinter import ttk, font
from tkinter import filedialog


class Aplication():

    def __init__(self):
        
        self.ventana = Tk()
        self.ventana.title("Dividir Videos")
        self.ventana.geometry("350x350")
        self.ventana.wm_resizable(0,0)

        #  Declarando la fuente de las letras
        fuente = font.Font(weight='bold')

        #  Etiquetas
        self.etiq1 = ttk.Label(self.ventana, text="Seleccione el video:", 
                               font=fuente)
        self.etiq3 = ttk.Label(self.ventana, text="Tiempo de recorrido (en segundos):",
                               font=fuente)

        # Variables de tipo string donde se guardaran los paths
        self.time = StringVar()

                
        # Define una cajas de entrada que aceptarán cadenas
        # de una longitud máxima de 5 caracteres para el tiempo
        self.ctext3 = ttk.Entry(self.ventana, 
                                textvariable=self.time,
                                width=5)
        self.separ1 = ttk.Separator(self.ventana, orient=HORIZONTAL)

        # Se definen dos botones con dos métodos: El botón
        # 'Aceptar' llamará al método 'self.split' cuando
        # sea presionado para dividir el video; y el botón
        # 'Cancelar' finalizará la aplicación si se llega a
        # presionar
        # Adicionalmente se agrego el boton seleccionar video, 
        # el cual mostarará un buscador de archivos para seleccionar el video a dividir
        
        self.botonV = ttk.Button(self.ventana, text="Seleccionar el video",
                                command=self.files)
        self.boton1 = ttk.Button(self.ventana, text="Aceptar", 
                                 command=self.split) 
        self.boton2 = ttk.Button(self.ventana, text="Cancelar", 
                                 command=self.close)

        self.etiq1.pack(side=TOP, fill=BOTH, expand=True, 
                        padx=5, pady=5)
        self.botonV.pack(side=TOP, fill=BOTH, expand=True, 
                        padx=5, pady=5)
        self.etiq3.pack(side=TOP, fill=BOTH, expand=True, 
                        padx=5, pady=5)
        self.ctext3.pack(side=TOP, fill=X, expand=True, 
                         padx=5, pady=5)
        self.separ1.pack(side=TOP, fill=BOTH, expand=True, 
                         padx=5, pady=5)
        self.boton1.pack(side=LEFT, fill=BOTH, expand=True, 
                         padx=5, pady=5)
        self.boton2.pack(side=RIGHT, fill=BOTH, expand=True, 
                         padx=5, pady=5)
        
        self.ventana.mainloop()

    # Metodo para obtener la direccion del video (URL) mediante un buscador de archivos
    def files(self):
        archivoEntrada = filedialog.askopenfilename(initialdir="/desktop", 
                                                    title = "Seleccione archivo", 
                                                    filetypes=(("mp4 files", "*.mp4"), 
                                                                ("All files", "*.*"))) 
        # print(archivoEntrada)
        self.pathIn = archivoEntrada
        self.etiq1.config(text="Video Seleccionado \n" + archivoEntrada, font="")

        self.pathOut = StringVar()
        self.pathOut = archivoEntrada.replace(".mp4", "")

    def split(self):
        pathDirIn = str(self.pathIn)
        pathDirOut = str(self.pathOut)
        timelapse = float(self.time.get())

        try:
            if not os.path.exists(pathDirOut):
                os.makedirs(pathDirOut)
        except OSError:
            self.close()
            pass

        saveDirFilenames = os.path.join(pathDirIn)
        print(saveDirFilenames)

        count = 1
        vidcap = cv2.VideoCapture(pathDirIn)


        frames = vidcap.get(cv2.CAP_PROP_FRAME_COUNT)
        fps = float(vidcap.get(cv2.CAP_PROP_FPS))

        # calculate duration of the video
        seconds = int(frames / fps)

        success,image = vidcap.read()
        while success and (count*timelapse*1000) <= (seconds*1000):            
            vidcap.set(cv2.CAP_PROP_POS_MSEC,(count*timelapse*1000))
            success,image = vidcap.read()
            cv2.imwrite( pathDirOut + "\\Fotograma%d.jpg" % count, image)
            count = count + 1

        cv2.destroyAllWindows()
        self.close()

    def close(self):
        self.ventana.destroy()

def main():
    Aplication()
    return 0

if __name__ == '__main__':
    main()
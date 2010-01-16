
NAME   = ecolux1115

CC      = arm-elf-gcc
LD      = arm-elf-ld -v
AR      = arm-elf-ar
AS      = arm-elf-as
CP      = arm-elf-objcopy
OD		= arm-elf-objdump

CFLAGS  = -I. -Iinc -ISTM32_USB/inc -ISTM32F10x_StdPeriph/inc -ICMSIS/Core/CM3 -I/usr/include -c -fno-common -O0 -g -mcpu=cortex-m3 -mthumb
AFLAGS  = -ahls -mapcs-32 -mcpu=cortex-m3 -mthumb 
LFLAGS  = -Tstm_h103_linker -nostartfiles
CPFLAGS = -Obinary
ODFLAGS	= -S

all: hexit

clean:
	-rm main.o main.out *.o 

hexit: main.out
	$(CP) $(CPFLAGS) main.out main.bin
	$(OD) $(ODFLAGS) main.out > main.list

main.out: main.o  startup_stm32f10x_ld.o misc.o hw_config.o usb_init.o usb_endp.o usb_prop.o usb_istr.o usb_desc.o usb_int.o usb_pwr.o usb_mem.o usb_sil.o usb_core.o usb_regs.o stm32f10x_rcc.o stm32f10x_flash.o stm32f10x_gpio.o stm32f10x_usart.o stm32f10x_it.o system_stm32f10x.o stm32f10x_bkp.o stm32f10x_rtc.o stm32f10x_pwr.o printf.o
	$(LD) $(LFLAGS) -o main.out startup_stm32f10x_ld.o main.o misc.o hw_config.o usb_init.o usb_istr.o usb_desc.o usb_int.o usb_mem.o usb_endp.o usb_prop.o usb_sil.o usb_core.o usb_pwr.o usb_regs.o stm32f10x_rcc.o stm32f10x_flash.o stm32f10x_gpio.o stm32f10x_usart.o stm32f10x_it.o system_stm32f10x.o stm32f10x_bkp.o stm32f10x_rtc.o stm32f10x_pwr.o printf.o 

printf.o: src/printf.c
	 $(CC) $(CFLAGS) src/printf.c

main.o: src/main.c
	 $(CC) $(CFLAGS) src/main.c
	 
hw_config.o: src/hw_config.c
	 $(CC) $(CFLAGS) src/hw_config.c

usb_prop.o: src/usb_prop.c
	 $(CC) $(CFLAGS) src/usb_prop.c

usb_endp.o: src/usb_endp.c
	 $(CC) $(CFLAGS) src/usb_endp.c

usb_istr.o: src/usb_istr.c
	 $(CC) $(CFLAGS) src/usb_istr.c
	 
usb_pwr.o: src/usb_pwr.c
	 $(CC) $(CFLAGS) src/usb_pwr.c

usb_desc.o: src/usb_desc.c
	 $(CC) $(CFLAGS) src/usb_desc.c

stm32f10x_it.o: src/stm32f10x_it.c
	 $(CC) $(CFLAGS) src/stm32f10x_it.c

######### CMSIS Core CM3 LIBRARY ######### 

startup_stm32f10x_ld.o: CMSIS/Core/CM3/startup_stm32f10x_ld.s
	$(AS) $(AFLAGS) -o startup_stm32f10x_ld.o CMSIS/Core/CM3/startup_stm32f10x_ld.s > startup_stm32f10x_ld.lst

system_stm32f10x.o: CMSIS/Core/CM3/system_stm32f10x.c
	$(CC) $(CFLAGS) CMSIS/Core/CM3/system_stm32f10x.c

######### STM32 USB LIBRARY ######### 

usb_init.o: STM32_USB/src/usb_init.c
	 $(CC) $(CFLAGS) STM32_USB/src/usb_init.c

usb_regs.o: STM32_USB/src/usb_regs.c
	 $(CC) $(CFLAGS) STM32_USB/src/usb_regs.c

usb_sil.o: STM32_USB/src/usb_sil.c
	 $(CC) $(CFLAGS) STM32_USB/src/usb_sil.c

usb_core.o: STM32_USB/src/usb_core.c
	 $(CC) $(CFLAGS) STM32_USB/src/usb_core.c

usb_mem.o: STM32_USB/src/usb_mem.c
	 $(CC) $(CFLAGS) STM32_USB/src/usb_mem.c
	 
usb_int.o: STM32_USB/src/usb_int.c
	 $(CC) $(CFLAGS) STM32_USB/src/usb_int.c
	 
######### STANDARD PERIPHERAL LIBRARY #########  	 
misc.o: STM32F10x_StdPeriph/src/misc.c
	 $(CC) $(CFLAGS) STM32F10x_StdPeriph/src/misc.c
	 
stm32f10x_rcc.o: STM32F10x_StdPeriph/src/stm32f10x_rcc.c
	 $(CC) $(CFLAGS) STM32F10x_StdPeriph/src/stm32f10x_rcc.c

stm32f10x_flash.o: STM32F10x_StdPeriph/src/stm32f10x_flash.c
	 $(CC) $(CFLAGS) STM32F10x_StdPeriph/src/stm32f10x_flash.c

stm32f10x_gpio.o: STM32F10x_StdPeriph/src/stm32f10x_gpio.c
	 $(CC) $(CFLAGS) STM32F10x_StdPeriph/src/stm32f10x_gpio.c	 
	 
stm32f10x_usart.o: STM32F10x_StdPeriph/src/stm32f10x_usart.c
	 $(CC) $(CFLAGS) STM32F10x_StdPeriph/src/stm32f10x_usart.c	 

stm32f10x_bkp.o: STM32F10x_StdPeriph/src/stm32f10x_bkp.c
	 $(CC) $(CFLAGS) STM32F10x_StdPeriph/src/stm32f10x_bkp.c	 
	 
stm32f10x_rtc.o: STM32F10x_StdPeriph/src/stm32f10x_rtc.c
	 $(CC) $(CFLAGS) STM32F10x_StdPeriph/src/stm32f10x_rtc.c	 

stm32f10x_pwr.o: STM32F10x_StdPeriph/src/stm32f10x_pwr.c
	 $(CC) $(CFLAGS) STM32F10x_StdPeriph/src/stm32f10x_pwr.c	 
	 
	 
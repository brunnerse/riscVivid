# ------------------------------------------------------------------
# Example program showing the use of data and text definitions.
# ------------------------------------------------------------------


        .data                   # begin of data section
num1:   .space  4               # reserve 4 bytes accessable via label number1
num2:   .space  4               # 4 bytes for number2 (can be used as variable)
num3:   .word   0x456           # similar to declaration with .space, but the
                                # memory is initialised with a value
ptr:    .word   num3            # pointer, points to address of num3


        .text                   # begin of code section
        .global main            # declares the main module and makes it visible

main:                           # entry point of program
        lw       t1, num3(zero) # loads content of num3 to register t1
        sw       t1, num2(zero) # writes content of register t1 to num2
        lw       t2, ptr(zero)  # loads the value of the pointer (addresse of
                                # num3) to register t2
        lw       t3, 0(t2)      # loads the value of num3 to register t3


# end of program
        add     a0, zero, 0     # exit code 0
        add     a7, zero, 93    # sycall 93: exit
        scall

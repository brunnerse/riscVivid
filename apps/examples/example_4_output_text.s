# ------------------------------------------------------------------
# Example program using syscall 64 for text output.
# ------------------------------------------------------------------

        .data
Text1:  .ascii "Hello World!"           # string of length 12

        .text
        .global main
main:
        add     a0, zero, 1             # a0=1 for stdout
        add     a1, zero, Text1         # a1= address of message
        add     a2, zero, 12            # a2= length of message
        add     a7, zero, 64            # syscall 64: write
                                        # This syscall prints a2 characters
                                        # starting at address a1
        scall

        add     a0, zero, 0             # exit code 0
        add     a7, zero, 93            # sycall 93: exit
        scall

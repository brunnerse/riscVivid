# ------------------------------------------------------------------
# Example program showing the use trap 5 for integer output.
# ------------------------------------------------------------------

                .data
_pi_buf:        .space 12
                .align 2
const1e9:       .word 1000000000
number:         .word 1234567890


        .text
        .global main
main:
        lw      a0, number(zero)
        jal     print_int

        add     a0, zero, 0             # exit code 0
        add     a7, zero, 93            # sycall 93: exit
        scall




# subroutine to print a string
# a0= number to print

print_int:
        add     t0, zero, 10
        add     t2, zero, 0
        beq     a0, zero, _pi_print     # special case: print "0"
        add     t2, zero, 9             # a2= maximum length of output string
        lw      t1, const1e9(zero)      # t1= 10^9 = 1000000000
        bltu    t1, a0, _pi_print       # print all 10 digits if >10^9

_pi_getlen:                             # determine length of output
        add     t2, t2, -1
        div     t1, t1, t0
        bltu    a0, t1, _pi_getlen

_pi_print:
        add     a2, t2, 1               # set length of output string

_pi_loop:
        remu    t1, a0, t0
        divu    a0, a0, t0
        add     t1, t1, 0x30
        sb      t1, _pi_buf(t2)
        add     t2, t2, -1
        bge     t2, zero, _pi_loop

        add     a0, zero, 1             # stdout
        add     a1, zero, _pi_buf       # addr
                                        # len already in a2
        add     a7, zero, 64            # write
        scall
        ret

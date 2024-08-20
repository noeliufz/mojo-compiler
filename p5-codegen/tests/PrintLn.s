	.text
PrintLn.println:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rdi,%rax
L.6:
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je PrintLn.println.badPtr
L.1:
	movq 0(%rbx),%rax
	movq %rax,%rdi
	call _puts
#	movq %rax,%rax
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
PrintLn.println.badPtr:
	call badPtr
	.data
	.balign 8
L.2:
	.byte 72
	.byte 101
	.byte 108
	.byte 108
	.byte 111
	.byte 32
	.byte 87
	.byte 111
	.byte 114
	.byte 108
	.byte 100
	.byte -30
	.byte -100
	.byte -123
	.byte 0
	.data
	.balign 8
L.3:
	.quad L.2
	.quad 15
	.text
PrintLn:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
L.7:
	leaq L.3(%rip),%rax
	movq %rax,%rdi
	call PrintLn.println
#	movq %rax,%rax
#	returnSink
	addq $16,%rsp
	popq %rbp
	ret
	.globl _main
	.text
_main:
	pushq %rbp
	movq %rsp,%rbp
L.8:
	call PrintLn
#	movq %rax,%rax
	xorq %rax,%rax
#	movq %rax,%rax
#	returnSink
	popq %rbp
	ret
	.data
	.balign 8
L.4:
	.byte 65
	.byte 116
	.byte 116
	.byte 101
	.byte 109
	.byte 112
	.byte 116
	.byte 32
	.byte 116
	.byte 111
	.byte 32
	.byte 117
	.byte 115
	.byte 101
	.byte 32
	.byte 97
	.byte 32
	.byte 110
	.byte 117
	.byte 108
	.byte 108
	.byte 32
	.byte 112
	.byte 111
	.byte 105
	.byte 110
	.byte 116
	.byte 101
	.byte 114
	.byte 0
	.text
badPtr:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
L.9:
	leaq L.4(%rip),%rax
	movq %rax,%rdi
	call _puts
#	movq %rax,%rax
	movq $1,%rax
	movq %rax,%rdi
	call _exit
#	movq %rax,%rax
#	returnSink
	addq $16,%rsp
	popq %rbp
	ret
	.data
	.balign 8
L.5:
	.byte 83
	.byte 117
	.byte 98
	.byte 115
	.byte 99
	.byte 114
	.byte 105
	.byte 112
	.byte 116
	.byte 32
	.byte 111
	.byte 117
	.byte 116
	.byte 32
	.byte 111
	.byte 102
	.byte 32
	.byte 98
	.byte 111
	.byte 117
	.byte 110
	.byte 100
	.byte 115
	.byte 0
	.text
badSub:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
L.10:
	leaq L.5(%rip),%rax
	movq %rax,%rdi
	call _puts
#	movq %rax,%rax
	movq $1,%rax
	movq %rax,%rdi
	call _exit
#	movq %rax,%rax
#	returnSink
	addq $16,%rsp
	popq %rbp
	ret

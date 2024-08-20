	.text
LargeLocal:
	pushq %rbp
	movq %rsp,%rbp
	subq $-2147483648,%rsp
	movq %rbx, -2147483648(%rbp)
L.4:
	movq $268435455,%rbx
	movq %rbx,%rsi
	xorq %rbx,%rbx
	movq %rbx,%rdx
	cmpq %rsi,%rdx
	jge L.1
L.0:
	movq %rdx,%rbx
	imulq $8,%rbx
	movq %rbp,%rcx
	addq %rbx,%rcx
	xorq %rbx,%rbx
	movq %rbx,-2147483640(%rcx)
	movq %rdx,%rbx
	addq $1,%rbx
	movq %rbx,%rdx
	cmpq %rsi,%rdx
	jl L.0
L.1:
#	returnSink
	movq -2147483648(%rbp),%rbx
	addq $-2147483648,%rsp
	popq %rbp
	ret
	.globl _main
	.text
_main:
	pushq %rbp
	movq %rsp,%rbp
L.5:
	call LargeLocal
#	movq %rax,%rax
	xorq %rax,%rax
#	movq %rax,%rax
#	returnSink
	popq %rbp
	ret
	.data
	.balign 8
L.2:
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
L.6:
	leaq L.2(%rip),%rax
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
L.3:
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
L.7:
	leaq L.3(%rip),%rax
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

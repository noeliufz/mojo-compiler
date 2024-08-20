	.text
FastFib.read_number:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %r12, -16(%rbp)
	movq %rbx, -8(%rbp)
L.26:
	xorq %rax,%rax
	movq %rax,%r12
L.1:
L.2:
	call _getchar
	movq %rax,%rbx
	movq $10,%rax
	cmpq %rax,%rbx
	je L.4
L.7:
	movq $13,%rax
	cmpq %rax,%rbx
	je L.4
L.5:
	movq $10,%rax
	movq %r12,%rcx
	imulq %rax,%rcx
	movq $48,%rax
#	movq %rbx,%rbx
	subq %rax,%rbx
	movq %rcx,%rax
	addq %rbx,%rax
	movq %rax,%r12
L.6:
	jmp L.1
L.4:
L.3:
	movq %r12,%rax
	jmp L.0
L.27:
	jmp L.6
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	movq -16(%rbp),%r12
	addq $16,%rsp
	popq %rbp
	ret
FastFib.read_number.badSub:
	call badSub
	.text
FastFib.fib:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rdi,%rsi
L.28:
#	movq %rsi,%rsi
	xorq %rax,%rax
	movq %rax,%rdx
	movq $1,%rax
	movq %rax,%rcx
	xorq %rax,%rax
	movq %rax,%rbx
	movq $1,%rax
	cmpq %rax,%rsi
	jl L.9
L.10:
L.11:
	movq $1,%rax
	cmpq %rax,%rsi
	jg L.12
L.13:
	movq %rcx,%rax
	jmp L.8
L.9:
	xorq %rax,%rax
#	movq %rax,%rax
	jmp L.8
L.12:
	movq %rcx,%rbx
	movq %rdx,%rax
	addq %rcx,%rax
	movq %rax,%rcx
	movq %rbx,%rdx
	movq $1,%rbx
	movq %rsi,%rax
	subq %rbx,%rax
	movq %rax,%rsi
	jmp L.11
L.8:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
FastFib.fib.badSub:
	call badSub
	.text
FastFib.CString:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rdi,%rax
L.29:
#	movq %rax,%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je FastFib.CString.badPtr
L.17:
	movq %rbx,%rcx
	xorq %rax,%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	jl FastFib.CString.badSub
L.18:
	movq 8(%rcx),%rax
	cmpq %rax,%rbx
	jge FastFib.CString.badSub
L.19:
	movq 0(%rcx),%rax
#	movq %rax,%rax
	addq %rbx,%rax
#	movq %rax,%rax
L.14:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
FastFib.CString.badPtr:
	call badPtr
FastFib.CString.badSub:
	call badSub
	.data
	.balign 8
L.20:
	.byte 73
	.byte 110
	.byte 112
	.byte 117
	.byte 116
	.byte 32
	.byte 110
	.byte 117
	.byte 109
	.byte 98
	.byte 101
	.byte 114
	.byte 58
	.byte 32
	.byte 0
	.data
	.balign 8
L.21:
	.quad L.20
	.quad 15
	.data
	.balign 8
L.22:
	.byte 37
	.byte 108
	.byte 100
	.byte 10
	.byte 0
	.data
	.balign 8
L.23:
	.quad L.22
	.quad 5
	.text
FastFib:
	pushq %rbp
	movq %rsp,%rbp
	subq $32,%rsp
	movq %r12, -16(%rbp)
	movq %rbx, -8(%rbp)
L.30:
	xorq %rax,%rax
	movq %rax,%r12
	leaq L.21(%rip),%rax
	movq %rax,%rdi
	call FastFib.CString
#	movq %rax,%rax
	movq %rax,%rdi
	xorq %rax,%rax
	movq %rax,%rsi
	call _printf
#	movq %rax,%rax
	call FastFib.read_number
	movq %rax,%r12
	leaq L.23(%rip),%rax
	movq %rax,%rdi
	call FastFib.CString
	movq %rax,%rbx
	movq %r12,%rdi
	call FastFib.fib
#	movq %rax,%rax
	movq %rbx,%rdi
	movq %rax,%rsi
	call _printf
#	movq %rax,%rax
#	returnSink
	movq -8(%rbp),%rbx
	movq -16(%rbp),%r12
	addq $32,%rsp
	popq %rbp
	ret
FastFib.badSub:
	call badSub
	.globl _main
	.text
_main:
	pushq %rbp
	movq %rsp,%rbp
L.31:
	call FastFib
#	movq %rax,%rax
	xorq %rax,%rax
#	movq %rax,%rax
#	returnSink
	popq %rbp
	ret
	.data
	.balign 8
L.24:
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
L.32:
	leaq L.24(%rip),%rax
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
L.25:
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
L.33:
	leaq L.25(%rip),%rax
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
